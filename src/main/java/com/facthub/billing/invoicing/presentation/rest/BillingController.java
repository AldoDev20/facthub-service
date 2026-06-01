package com.facthub.billing.invoicing.presentation.rest;

import com.facthub.billing.invoicing.application.dto.InvoiceRequestDto;
import com.facthub.billing.invoicing.application.usecase.GenerateInvoiceUseCase;
import com.facthub.billing.invoicing.domain.model.Invoice;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class BillingController {

    private final GenerateInvoiceUseCase generateInvoiceUseCase;

    public BillingController(GenerateInvoiceUseCase generateInvoiceUseCase) {
        this.generateInvoiceUseCase = generateInvoiceUseCase;
    }

    /**
     * Emits a new invoice to SUNAT.
     *
     * @param request the invoice request DTO
     * @return response with invoice data and SUNAT status
     */
    @PostMapping(value = "/issue")
    public ResponseEntity<Map<String, Object>> issueInvoice(@Valid @RequestBody InvoiceRequestDto request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Execute the use case to generate, sign, and send invoice
            Invoice invoice = generateInvoiceUseCase.execute(request);

            // Build response
            response.put("success", true);
            response.put("message", "Invoice issued successfully");

            Map<String, Object> invoiceInfo = new HashMap<>();
            invoiceInfo.put("id", invoice.getId());
            invoiceInfo.put("series", invoice.getSeries());
            invoiceInfo.put("number", invoice.getNumber());
            invoiceInfo.put("customerDocumentNumber", invoice.getCustomerRuc());
            invoiceInfo.put("customerName", invoice.getCustomerName());
            invoiceInfo.put("totalAmount", invoice.getTotalAmount());
            invoiceInfo.put("issueDate", invoice.getIssueDate());

            Map<String, Object> sunatInfo = new HashMap<>();
            sunatInfo.put("status", invoice.getSunatStatus());
            sunatInfo.put("ticket", invoice.getSunatTicket());
            sunatInfo.put("xmlContent", invoice.getXmlContent());

            response.put("invoice", invoiceInfo);
            response.put("sunat", sunatInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error issuing invoice");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Health check endpoint.
     *
     * @return service status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "facthub-billing-service");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
