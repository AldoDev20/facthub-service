package com.facthub.billing.billing.infrastructure.xbuilder;

import com.facthub.billing.billing.application.dto.InvoiceRequestDto;
import com.facthub.billing.billing.application.dto.ItemDto;
import com.facthub.billing.billing.domain.model.Invoice;
import com.facthub.billing.directory.domain.model.Taxpayer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XBuilderBillingServiceTest {

    @Test
    void testGenerateAndSignInvoiceXml() throws Exception {
        // Arrange
        XBuilderBillingService service = new XBuilderBillingService();

        Invoice invoice = new Invoice();
        invoice.setNumber(1);
        invoice.setSeries("F001");

        ItemDto item1 = new ItemDto();
        item1.setDescription("Software Development");
        item1.setQuantity(new BigDecimal("1.0"));
        item1.setUnitPrice(new BigDecimal("1500.00"));

        InvoiceRequestDto request = new InvoiceRequestDto();
        request.setIssuerRuc("20123456789");
        request.setDocumentType("INVOICE");
        request.setCustomerDocumentType("RUC");
        request.setCustomerDocumentNumber("20100078941");
        request.setItems(List.of(item1));

        Taxpayer taxpayer = Taxpayer.builder()
                .ruc("20100078941")
                .nombre("ACME PERU S.A.C.")
                .estado("ACTIVO")
                .condicionDomicilio("HABIDO")
                .build();

        // Create mock Company for test
        byte[] certBytes;
        try (java.io.InputStream is = new org.springframework.core.io.ClassPathResource("certificado-prueba.pfx").getInputStream()) {
            certBytes = is.readAllBytes();
        }
        com.facthub.billing.company.domain.model.Company company = com.facthub.billing.company.domain.model.Company.builder()
                .ruc("20123456789")
                .businessName("MiEmpresa")
                .certificateContent(certBytes)
                .certificatePassword("miclave")
                .build();

        // Act
        String signedXml = service.generateAndSignInvoiceXml(invoice, request, taxpayer, company);

        // Assert
        assertNotNull(signedXml, "Signed XML should not be null");
        
        // Verify basic UBL structure
        assertTrue(signedXml.contains("01</cbc:InvoiceTypeCode>"), 
                "XML should contain document type 01 (Invoice)");
        assertTrue(signedXml.contains("ACME PERU S.A.C."), 
                "XML should contain customer name");
        assertTrue(signedXml.contains("1500.00"), 
                "XML should contain item price");
        
        // Verify digital signature
        assertTrue(signedXml.contains("<ds:Signature Id=\"MiEmpresa\""), 
                "XML should be signed with company identifier");
    }
}
