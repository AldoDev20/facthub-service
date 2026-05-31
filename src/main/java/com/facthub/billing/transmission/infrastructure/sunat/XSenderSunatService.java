package com.facthub.billing.transmission.infrastructure.sunat;

import org.springframework.stereotype.Service;

/**
 * Service for sending invoices to SUNAT using XSender.
 * Implementation will be completed when we have actual XML to send.
 */
@Service
public class XSenderSunatService {

    /**
     * Sends signed XML invoice to SUNAT via SOAP.
     *
     * @param xmlFirmado the signed XML content
     * @return response from SUNAT (placeholder for now)
     */
    public Object enviarFactura(String xmlFirmado) {
        // Pending: Implement actual XSender integration
        // This requires correct package imports from spring-boot-xsender runtime
        return null;
    }
}
