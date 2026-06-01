package com.facthub.billing.billing.application.usecase;

import com.facthub.billing.billing.application.dto.InvoiceRequestDto;
import com.facthub.billing.billing.domain.model.Invoice;
import com.facthub.billing.billing.domain.model.InvoiceSequence;
import com.facthub.billing.billing.domain.repository.InvoiceRepository;
import com.facthub.billing.billing.domain.repository.InvoiceSequenceRepository;
import com.facthub.billing.billing.infrastructure.xbuilder.XBuilderBillingService;
import com.facthub.billing.directory.domain.model.Taxpayer;
import com.facthub.billing.directory.application.usecase.GetTaxpayerInfoUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class GenerateInvoiceUseCase {

    private final GetTaxpayerInfoUseCase getTaxpayerInfoUseCase;
    private final com.facthub.billing.company.application.usecase.GetCompanyByRucUseCase getCompanyByRucUseCase;
    private final XBuilderBillingService xBuilderService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceSequenceRepository sequenceRepository;

    public GenerateInvoiceUseCase(
            GetTaxpayerInfoUseCase getTaxpayerInfoUseCase,
            com.facthub.billing.company.application.usecase.GetCompanyByRucUseCase getCompanyByRucUseCase,
            XBuilderBillingService xBuilderService,
            InvoiceRepository invoiceRepository,
            InvoiceSequenceRepository sequenceRepository) {
        this.getTaxpayerInfoUseCase = getTaxpayerInfoUseCase;
        this.getCompanyByRucUseCase = getCompanyByRucUseCase;
        this.xBuilderService = xBuilderService;
        this.invoiceRepository = invoiceRepository;
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates, signs, and sends an invoice to SUNAT.
     *
     * @param request the invoice request DTO
     * @return the created Invoice entity with SUNAT status
     */
    @Transactional
    public Invoice execute(InvoiceRequestDto request) {
        // 1. Determine Document Type and Series
        boolean isInvoice = "INVOICE".equalsIgnoreCase(request.getDocumentType());
        String series = isInvoice ? "F001" : "B001";
        String documentType = isInvoice ? "01" : "03";

        // 2. Validate taxpayer
        Taxpayer taxpayer;
        if (isInvoice) {
            taxpayer = getTaxpayerInfoUseCase.execute(request.getCustomerDocumentNumber());
        } else {
            // For Boleta/RECEIPT, we don't query Searchpe, we just use the provided data
            if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
                throw new RuntimeException("customerName is required to issue a RECEIPT.");
            }
            taxpayer = new Taxpayer();
            taxpayer.setRuc(request.getCustomerDocumentNumber());
            taxpayer.setNombre(request.getCustomerName());
        }

        // 3. Validate Issuer Company
        com.facthub.billing.company.domain.model.Company company = getCompanyByRucUseCase.execute(request.getIssuerRuc());

        // 4. Generate sequence number
        int invoiceNumber = getNextNumber(series);

        // 5. Create invoice record in PENDING state
        Invoice invoice = Invoice.builder()
                .documentType(documentType)
                .issuerRuc(company.getRuc())
                .series(series)
                .number(invoiceNumber)
                .customerRuc(taxpayer.getRuc())
                .customerName(taxpayer.getNombre())
                .totalAmount(calculateTotal(request))
                .issueDate(LocalDateTime.now())
                .sunatStatus("PENDING")
                .build();

        // 5. Generate and sign XML
        String signedXml;
        try {
            signedXml = xBuilderService.generateAndSignInvoiceXml(invoice, request, taxpayer, company);
        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice XML: " + e.getMessage(), e);
        }

        // Save invoice
        invoice = invoiceRepository.save(invoice);

        // 6. Send to SUNAT (SIMULADO)
        // Omitimos la llamada real a sendToSunatUseCase para evitar errores por datos ficticios.
        // Asignamos directamente el XML a la base de datos
        invoice.setXmlContent(signedXml);

        // 7. Update invoice with SUNAT response
        // BURLAMOS A LA SUNAT PARA LA PRESENTACIÓN UNIVERSITARIA:
        // Forzamos el estado a "ACCEPTED" y asignamos un ticket falso.
        invoice.setSunatStatus("ACCEPTED");
        invoice.setSunatTicket("TICKET-MOCK-123456");
        
        invoice = invoiceRepository.save(invoice);

        return invoice;
    }

    private BigDecimal calculateTotal(InvoiceRequestDto request) {
        return request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int getNextNumber(String series) {
        int updatedRows = sequenceRepository.incrementAndGetNumber(series);
        if (updatedRows == 0) {
            throw new RuntimeException("Invoice series not found: " + series);
        }
        InvoiceSequence sequence = sequenceRepository.findById(series)
                .orElseThrow(() -> new RuntimeException("Error getting correlative for series: " + series));
        return sequence.getLastNumber();
    }
}
