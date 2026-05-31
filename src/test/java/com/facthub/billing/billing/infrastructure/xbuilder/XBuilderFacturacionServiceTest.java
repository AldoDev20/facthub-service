package com.facthub.billing.billing.infrastructure.xbuilder;

import com.facthub.billing.billing.application.dto.FacturaRequestDto;
import com.facthub.billing.billing.application.dto.ItemDto;
import com.facthub.billing.billing.domain.model.Invoice;
import com.facthub.billing.directory.domain.model.Taxpayer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XBuilderFacturacionServiceTest {

    @Test
    void testGenerarYFirmarFacturaXml() throws Exception {
        // Arrange
        XBuilderFacturacionService service = new XBuilderFacturacionService();

        Invoice invoice = new Invoice();
        invoice.setNumber(1);

        ItemDto item1 = new ItemDto();
        item1.setDescripcion("Desarrollo de Software");
        item1.setCantidad(new BigDecimal("1.0"));
        item1.setPrecioUnitario(new BigDecimal("1500.00"));

        FacturaRequestDto request = new FacturaRequestDto();
        request.setRucEmisor("20123456789");
        request.setTipoComprobante("FACTURA");
        request.setTipoDocumentoCliente("RUC");
        request.setNumeroDocumentoCliente("20100078941");
        request.setItems(List.of(item1));

        Taxpayer taxpayer = Taxpayer.builder()
                .ruc("20100078941")
                .nombre("ACME PERU S.A.C.")
                .estado("ACTIVO")
                .condicionDomicilio("HABIDO")
                .build();

        // Crear Company simulado para el test
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
        String xmlFirmado = service.generarYFirmarFacturaXml(invoice, request, taxpayer, company);

        // Assert
        assertNotNull(xmlFirmado, "El XML firmado no debe ser nulo");
        
        // Verificar estructura UBL básica
        assertTrue(xmlFirmado.contains("01</cbc:InvoiceTypeCode>"), 
                "El XML debe contener el tipo de comprobante 01 (Factura)");
        assertTrue(xmlFirmado.contains("ACME PERU S.A.C."), 
                "El XML debe contener el nombre del cliente");
        assertTrue(xmlFirmado.contains("1500.00"), 
                "El XML debe contener el precio del ítem");
        
        // Verificar firma digital
        assertTrue(xmlFirmado.contains("<ds:Signature Id=\"MiEmpresa\""), 
                "El XML debe estar firmado con el identificador de la empresa");
    }
}
