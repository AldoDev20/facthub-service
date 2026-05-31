package com.facthub.billing.billing.application.usecase;

import com.facthub.billing.billing.application.dto.FacturaRequestDto;
import com.facthub.billing.billing.domain.model.Invoice;
import com.facthub.billing.billing.domain.repository.InvoiceRepository;
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
    private final XBuilderFacturacionService xBuilderService;
    private final SendInvoiceToSunatUseCase sendToSunatUseCase;
    private final InvoiceRepository invoiceRepository;

    public GenerateInvoiceUseCase(
            GetTaxpayerInfoUseCase getTaxpayerInfoUseCase,
            XBuilderFacturacionService xBuilderService,
            SendInvoiceToSunatUseCase sendToSunatUseCase,
            InvoiceRepository invoiceRepository) {
        this.getTaxpayerInfoUseCase = getTaxpayerInfoUseCase;
        this.xBuilderService = xBuilderService;
        this.sendToSunatUseCase = sendToSunatUseCase;
        this.invoiceRepository = invoiceRepository;
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

        // 2. Generate and sign XML
        String xmlFirmado;
        try {
            xmlFirmado = xBuilderService.generarYFirmarFacturaXml(request, taxpayer);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar XML de factura: " + e.getMessage(), e);
        }

        // 3. Create invoice record in PENDING state
        Invoice invoice = Invoice.builder()
                .documentType("01") // 01 = Factura
                .series("F001")
                .number(0) // Will be set by sequence
                .customerRuc(taxpayer.getRuc())
                .customerName(taxpayer.getNombre())
                .totalAmount(calculateTotal(request))
                .issueDate(LocalDateTime.now())
                .sunatStatus("PENDING")
                .build();

        // Save invoice
        invoice = invoiceRepository.save(invoice);

        // 4. Send to SUNAT
        SunatTicket sunatTicket = sendToSunatUseCase.execute(xmlFirmado);

        // 5. Update invoice with SUNAT response
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
}
