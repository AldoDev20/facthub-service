package com.facthub.billing.billing.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequestDto {

    /** Issuer RUC */
    @NotBlank(message = "Issuer RUC is required")
    private String issuerRuc;

    /** Type of document (INVOICE or RECEIPT) */
    @NotBlank(message = "Document type is required (INVOICE or RECEIPT)")
    private String documentType;

    /** Customer document type (RUC, DNI, CE, PASAPORTE, SIN_DOCUMENTO) */
    @NotBlank(message = "Customer document type is required (RUC, DNI, CE)")
    private String customerDocumentType;

    /** Customer document number */
    @NotBlank(message = "Customer document number is required")
    private String customerDocumentNumber;

    /** Customer full name (Required for RECEIPT/Boleta, Optional for INVOICE/Factura) */
    private String customerName;

    /** List of items/products */
    @NotEmpty(message = "Invoice must have at least one item")
    @Valid
    private List<ItemDto> items;
}
