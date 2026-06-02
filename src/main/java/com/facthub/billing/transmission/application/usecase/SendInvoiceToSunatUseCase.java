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
     * @param signedXml the signed XML content
     * @param company the issuer company containing credentials
     * @return SunatTicket with transmission result
     */
    public SunatTicket execute(String signedXml, com.facthub.billing.company.domain.model.Company company) {
        try {
            SunatResponse response = sunatService.sendInvoice(signedXml, company);

            return SunatTicket.builder()
                    .status(response.getStatus() != null ? response.getStatus().toString() : "UNKNOWN")
                    .ticket(response.getSunat() != null ? response.getSunat().getTicket() : null)
                    .cdrContent(response.getSunat() != null ? response.getSunat().getCdr() : null)
                    .description("XML sent to SUNAT")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error sending invoice to SUNAT: " + e.getMessage(), e);
        }
    }
}
