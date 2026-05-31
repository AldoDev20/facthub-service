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
import org.springframework.stereotype.Service;

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
    public String generarYFirmarFacturaXml(Invoice invoice, FacturaRequestDto request, Taxpayer taxpayer) throws Exception {
        int numeroFactura = invoice.getNumber();

        // 1. Build Provider
        Proveedor proveedor = Proveedor.builder()
                .ruc("20123456789")
                .razonSocial("EMPRESA EMISORA DE PRUEBA S.A.C.")
                .build();

        // 2. Build Customer
        Cliente cliente = Cliente.builder()
                .nombre(taxpayer.getNombre())
                .numeroDocumentoIdentidad(taxpayer.getRuc())
                .tipoDocumentoIdentidad(Catalog6.RUC.toString())
                .build();

        // 3. Build Invoice using XBuilder
        io.github.project.openubl.xbuilder.content.models.standard.general.Invoice.InvoiceBuilder xbuilderInvoice = 
                io.github.project.openubl.xbuilder.content.models.standard.general.Invoice.builder()
                .serie("F001")
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
        return TemplateProducer.getInstance().getInvoice().data(input).render();
    }

}
