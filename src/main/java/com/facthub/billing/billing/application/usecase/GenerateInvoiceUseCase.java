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
        // 1. Determine Document Type and Series
        boolean isFactura = "FACTURA".equalsIgnoreCase(request.getTipoComprobante());
        String serie = isFactura ? "F001" : "B001";
        String documentType = isFactura ? "01" : "03";

        // 2. Validate taxpayer
        Taxpayer taxpayer;
        if (isFactura) {
            taxpayer = getTaxpayerInfoUseCase.execute(request.getNumeroDocumentoCliente());
        } else {
            // For Boleta, we don't query Searchpe, we just use the provided data
            if (request.getNombreCliente() == null || request.getNombreCliente().trim().isEmpty()) {
                throw new RuntimeException("El nombreCliente es obligatorio para emitir Boletas.");
            }
            taxpayer = new Taxpayer();
            taxpayer.setRuc(request.getNumeroDocumentoCliente());
            taxpayer.setNombre(request.getNombreCliente());
        }

        // 3. Validate Issuer Company
        com.facthub.billing.company.domain.model.Company company = getCompanyByRucUseCase.execute(request.getRucEmisor());

        // 4. Generate sequence number
        int numeroFactura = obtenerSiguienteNumero(serie);

        // 5. Create invoice record in PENDING state
        Invoice invoice = Invoice.builder()
                .documentType(documentType)
                .issuerRuc(company.getRuc())
                .series(serie)
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
