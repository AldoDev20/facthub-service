package com.facthub.billing.billing.infrastructure.xbuilder;

import com.facthub.billing.billing.application.dto.FacturaRequestDto;
import com.facthub.billing.billing.domain.model.Invoice;
import com.facthub.billing.directory.domain.model.Taxpayer;
import io.github.project.openubl.xbuilder.content.catalogs.Catalog6;
import io.github.project.openubl.xbuilder.content.models.common.Cliente;
import io.github.project.openubl.xbuilder.content.models.common.Proveedor;
import io.github.project.openubl.xbuilder.content.models.standard.general.DocumentoVentaDetalle;
import io.github.project.openubl.xbuilder.enricher.ContentEnricher;
import io.github.project.openubl.xbuilder.enricher.config.DateProvider;
import io.github.project.openubl.xbuilder.enricher.config.Defaults;
import io.github.project.openubl.xbuilder.renderer.TemplateProducer;
import io.github.project.openubl.xbuilder.signature.CertificateDetails;
import io.github.project.openubl.xbuilder.signature.CertificateDetailsFactory;
import io.github.project.openubl.xbuilder.signature.XMLSigner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service for generating and signing invoice XML using XBuilder.
 * Implementation will be completed with actual XBuilder API calls.
 */
@Service
public class XBuilderFacturacionService {

    /**
     * Generates and signs an invoice XML using XBuilder.
     *
     * @param invoice the invoice entity
     * @param request the invoice request DTO
     * @param taxpayer the validated taxpayer information
     * @return signed XML as String
     * @throws Exception if generation or signing fails
     */
    public String generarYFirmarFacturaXml(Invoice invoice, FacturaRequestDto request, Taxpayer taxpayer, com.facthub.billing.company.domain.model.Company company) throws Exception {
        int numeroFactura = invoice.getNumber();

        // 1. Build Provider dynamically from Company
        Proveedor proveedor = Proveedor.builder()
                .ruc(company.getRuc())
                .razonSocial(company.getBusinessName())
                .build();

        // 2. Map Catalog6 dynamically based on input
        String tipoDocInput = request.getTipoDocumentoCliente();
        String catalog6Code = "6"; // RUC by default
        if ("DNI".equalsIgnoreCase(tipoDocInput)) {
            catalog6Code = "1";
        } else if ("CE".equalsIgnoreCase(tipoDocInput)) {
            catalog6Code = "4";
        } else if ("PASAPORTE".equalsIgnoreCase(tipoDocInput)) {
            catalog6Code = "7";
        } else if ("SIN_DOCUMENTO".equalsIgnoreCase(tipoDocInput)) {
            catalog6Code = "0";
        }

        // 3. Build Customer
        Cliente cliente = Cliente.builder()
                .nombre(taxpayer.getNombre())
                .numeroDocumentoIdentidad(taxpayer.getRuc())
                .tipoDocumentoIdentidad(catalog6Code)
                .build();

        // 4. Build Invoice using XBuilder dynamically (Factura or Boleta inferred from Serie)
        var xbuilderInvoice = 
                io.github.project.openubl.xbuilder.content.models.standard.general.Invoice.builder()
                .serie(invoice.getSeries())
                .numero(numeroFactura)
                .proveedor(proveedor)
                .cliente(cliente);

        // 4. Add items dynamically
        request.getItems().forEach(item -> {
            xbuilderInvoice.detalle(DocumentoVentaDetalle.builder()
                    .descripcion(item.getDescripcion())
                    .cantidad(item.getCantidad())
                    .precio(item.getPrecioUnitario())
                    .unidadMedida("NIU") // NIU = Unidades
                    .build());
        });

        io.github.project.openubl.xbuilder.content.models.standard.general.Invoice input = xbuilderInvoice.build();

        // 5. Configure Defaults & Enricher
        Defaults defaults = Defaults.builder()
                .igvTasa(new BigDecimal("0.18"))
                .build();
        DateProvider dateProvider = () -> LocalDate.now();

        ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
        enricher.enrich(input);

        // 6. Render Raw XML
        String xmlCrudo = TemplateProducer.getInstance().getInvoice().data(input).render();

        // 7. Cargar certificado desde la Base de Datos y FIRMAR el XML
        InputStream ksInputStream = new java.io.ByteArrayInputStream(company.getCertificateContent());
        CertificateDetails certificate = CertificateDetailsFactory.create(ksInputStream, company.getCertificatePassword());

        Document signedXML = XMLSigner.signXML(xmlCrudo, company.getBusinessName(), certificate.getX509Certificate(), certificate.getPrivateKey());

        // 8. Convertir el XML Firmado a String
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(signedXML), new StreamResult(writer));

        return writer.toString();
    }

}
