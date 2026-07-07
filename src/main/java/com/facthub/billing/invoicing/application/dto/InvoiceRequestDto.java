package com.facthub.billing.invoicing.application.dto;

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
    private String issuerRuc;

    /** Type of document (INVOICE or RECEIPT) */
    private String documentType;

    /** Customer document type (RUC, DNI, CE, PASAPORTE, SIN_DOCUMENTO) */
    private String customerDocumentType;

    /** Customer document number */
    private String customerDocumentNumber;

    /** Customer full name (Required for RECEIPT/Boleta, Optional for INVOICE/Factura) */
    private String customerName;

    /** List of items/products */
    private List<ItemDto> items;
}
