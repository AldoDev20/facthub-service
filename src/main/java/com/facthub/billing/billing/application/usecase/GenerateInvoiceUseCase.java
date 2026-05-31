package com.facthub.billing.billing.application.usecase;

import com.facthub.billing.billing.application.dto.FacturaRequestDto;
import com.facthub.billing.billing.domain.model.Invoice;
import com.facthub.billing.billing.domain.model.InvoiceSequence;
import com.facthub.billing.billing.domain.repository.InvoiceRepository;
import com.facthub.billing.billing.domain.repository.InvoiceSequenceRepository;
import com.facthub.billing.billing.infrastructure.xbuilder.XBuilderFacturacionService;
import com.facthub.billing.directory.domain.model.Taxpayer;
import com.facthub.billing.directory.application.usecase.GetTaxpayerInfoUseCase;
import com.facthub.billing.transmission.application.usecase.SendInvoiceToSunatUseCase;
import com.facthub.billing.transmission.domain.model.SunatTicket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class GenerateInvoiceUseCase {

    private final GetTaxpayerInfoUseCase getTaxpayerInfoUseCase;
    private final com.facthub.billing.company.application.usecase.GetCompanyByRucUseCase getCompanyByRucUseCase;
    private final XBuilderFacturacionService xBuilderService;
    private final SendInvoiceToSunatUseCase sendToSunatUseCase;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceSequenceRepository sequenceRepository;

    public GenerateInvoiceUseCase(
            GetTaxpayerInfoUseCase getTaxpayerInfoUseCase,
            com.facthub.billing.company.application.usecase.GetCompanyByRucUseCase getCompanyByRucUseCase,
            XBuilderFacturacionService xBuilderService,
            SendInvoiceToSunatUseCase sendToSunatUseCase,
            InvoiceRepository invoiceRepository,
            InvoiceSequenceRepository sequenceRepository) {
        this.getTaxpayerInfoUseCase = getTaxpayerInfoUseCase;
        this.getCompanyByRucUseCase = getCompanyByRucUseCase;
        this.xBuilderService = xBuilderService;
        this.sendToSunatUseCase = sendToSunatUseCase;
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
    public Invoice execute(FacturaRequestDto request) {
        // 1. Validate taxpayer
        Taxpayer taxpayer = getTaxpayerInfoUseCase.execute(request.getRucCliente());

        // 2. Validate Issuer Company
        com.facthub.billing.company.domain.model.Company company = getCompanyByRucUseCase.execute(request.getRucEmisor());

        // 3. Generate sequence number
        int numeroFactura = obtenerSiguienteNumero("F001");

        // 4. Create invoice record in PENDING state
        Invoice invoice = Invoice.builder()
                .documentType("01") // 01 = Factura
                .issuerRuc(company.getRuc())
                .series("F001")
                .number(numeroFactura)
                .customerRuc(taxpayer.getRuc())
                .customerName(taxpayer.getNombre())
                .totalAmount(calculateTotal(request))
                .issueDate(LocalDateTime.now())
                .sunatStatus("PENDING")
                .build();

        // 5. Generate and sign XML
        String xmlFirmado;
        try {
            xmlFirmado = xBuilderService.generarYFirmarFacturaXml(invoice, request, taxpayer, company);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar XML de factura: " + e.getMessage(), e);
        }

        // Save invoice
        invoice = invoiceRepository.save(invoice);

        // 6. Send to SUNAT
        SunatTicket sunatTicket = sendToSunatUseCase.execute(xmlFirmado, company);

        // 7. Update invoice with SUNAT response
        invoice.setSunatStatus(sunatTicket.isAccepted() ? "ACCEPTED" : "PENDING");
        invoice.setSunatTicket(sunatTicket.getTicket());
        invoice = invoiceRepository.save(invoice);

        return invoice;
    }

    private BigDecimal calculateTotal(FacturaRequestDto request) {
        return request.getItems().stream()
                .map(item -> item.getPrecioUnitario().multiply(item.getCantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int obtenerSiguienteNumero(String serie) {
        int updatedRows = sequenceRepository.incrementAndGetNumber(serie);
        if (updatedRows == 0) {
            throw new RuntimeException("Serie de factura no encontrada: " + serie);
        }
        InvoiceSequence sequence = sequenceRepository.findById(serie)
                .orElseThrow(() -> new RuntimeException("Error al obtener correlativo para serie: " + serie));
        return sequence.getLastNumber();
    }
}
