package com.facthub.billing.transmission.application.usecase;

import com.facthub.billing.transmission.domain.model.SunatTicket;
import com.facthub.billing.transmission.infrastructure.sunat.XSenderSunatService;
import io.github.project.openubl.xsender.models.SunatResponse;
import org.springframework.stereotype.Service;

@Service
public class SendInvoiceToSunatUseCase {

    private final XSenderSunatService sunatService;

    public SendInvoiceToSunatUseCase(XSenderSunatService sunatService) {
        this.sunatService = sunatService;
    }

    /**
     * Sends signed XML to SUNAT and returns ticket information.
     *
     * @param xmlFirmado the signed XML content
     * @return SunatTicket with transmission result
     */
    public SunatTicket execute(String xmlFirmado) {
        try {
            SunatResponse response = sunatService.enviarFactura(xmlFirmado);

            return SunatTicket.builder()
                    .status(response.getStatus() != null ? response.getStatus().toString() : "UNKNOWN")
                    .ticket(response.getSunat() != null ? response.getSunat().getTicket() : null)
                    .description("XML enviado a SUNAT")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar factura a SUNAT: " + e.getMessage(), e);
        }
    }
}
