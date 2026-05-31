package com.facthub.billing.billing.infrastructure.xbuilder;

import com.facthub.billing.billing.application.dto.FacturaRequestDto;
import com.facthub.billing.billing.domain.model.InvoiceSequence;
import com.facthub.billing.billing.domain.repository.InvoiceSequenceRepository;
import com.facthub.billing.directory.domain.model.Taxpayer;
import org.springframework.stereotype.Service;

/**
 * Service for generating and signing invoice XML using XBuilder.
 * Implementation will be completed with actual XBuilder API calls.
 */
@Service
public class XBuilderFacturacionService {

    private final InvoiceSequenceRepository sequenceRepository;

    public XBuilderFacturacionService(InvoiceSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates and signs an invoice XML using XBuilder.
     *
     * @param request the invoice request DTO
     * @param taxpayer the validated taxpayer information
     * @return signed XML as String
     * @throws Exception if generation or signing fails
     */
    public String generarYFirmarFacturaXml(FacturaRequestDto request, Taxpayer taxpayer) throws Exception {
        // 1. Obtener correlativo
        int numeroFactura = obtenerSiguienteNumero("F001");

        // Pending: Implement actual XBuilder integration
        // This requires exploring the XBuilder 5.0.2 API to get correct package names
        // Placeholder for now - will be completed in next iteration

        // Steps to implement:
        // 2. Construir cabecera usando XBuilder (Invoice.builder()...)
        // 3. Agregar los productos dinámicamente
        // 4. Enriquecer con ContentEnricher (cálculo automático de IGV, Total, etc)
        // 5. Renderizar a XML crudo con TemplateProducer
        // 6. Cargar certificado y firmar con XMLSigner
        // 7. Convertir Document a String

        String xmlPlaceholder = "<?xml version=\"1.0\"?><placeholder>XML pending implementation for F001-" + numeroFactura + "</placeholder>";
        return xmlPlaceholder;
    }

    /**
     * Gets the next invoice number atomically.
     *
     * @param serie the invoice series
     * @return the next number
     */
    private int obtenerSiguienteNumero(String serie) {
        int updatedRows = sequenceRepository.incrementAndGetNumber(serie);
        if (updatedRows == 0) {
            throw new RuntimeException("Serie de factura no encontrada: " + serie);
        }

        InvoiceSequence sequence = sequenceRepository.findById(serie)
                .orElseThrow(() -> new RuntimeException("Error al obtener correlativo para serie: " + serie));

        return sequence.getLastNumber();
    }
}
