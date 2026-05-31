package com.facthub.billing.transmission.infrastructure.sunat;

import io.github.project.openubl.xsender.camel.utils.CamelUtils;
import io.github.project.openubl.xsender.company.CompanyCredentials;
import io.github.project.openubl.xsender.company.CompanyURLs;
import io.github.project.openubl.xsender.Constants;
import io.github.project.openubl.xsender.files.BillServiceXMLFileAnalyzer;
import io.github.project.openubl.xsender.models.SunatResponse;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Service for sending invoices to SUNAT using XSender.
 */
@Service
public class XSenderSunatService {

    @Autowired
    private CamelContext camelContext;

    /**
     * Sends signed XML invoice to SUNAT via SOAP.
     *
     * @param xmlFirmado the signed XML content
     * @param company the issuer company containing credentials
     * @return response from SUNAT
     * @throws Exception if transmission fails
     */
    public SunatResponse enviarFactura(String xmlFirmado, com.facthub.billing.company.domain.model.Company company) throws Exception {
        // 1. Configurar URLs (Entorno Beta)
        // TODO: Mover entorno (Beta/Producción) a configuración de properties
        CompanyURLs companyURLs = CompanyURLs.builder()
                .invoice("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
                .build();

        // 2. Configurar Credenciales Dinámicas de la Empresa
        CompanyCredentials credentials = CompanyCredentials.builder()
                .username(company.getSunatSolUsername())
                .password(company.getSunatSolPassword())
                .build();

        // 3. Analizar XML y obtener datos para envío
        byte[] xmlBytes = xmlFirmado.getBytes(StandardCharsets.UTF_8);
        BillServiceXMLFileAnalyzer fileAnalyzer = new BillServiceXMLFileAnalyzer(xmlBytes, companyURLs);
        
        var zipFile = fileAnalyzer.getZipFile();
        var fileDestination = fileAnalyzer.getSendFileDestination();

        // 4. Construir datos de Camel
        var camelData = CamelUtils.getBillServiceCamelData(zipFile, fileDestination, credentials);

        // 5. Enviar a SUNAT usando CamelContext
        return camelContext.createProducerTemplate()
                .requestBodyAndHeaders(
                        Constants.XSENDER_BILL_SERVICE_URI,
                        camelData.getBody(),
                        camelData.getHeaders(),
                        SunatResponse.class
                );
    }
}
