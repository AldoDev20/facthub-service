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

    @org.springframework.beans.factory.annotation.Value("${sunat.environment.url:https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService}")
    private String sunatUrl;

    /**
     * Sends signed XML invoice to SUNAT via SOAP.
     *
     * @param signedXml the signed XML content
     * @param company the issuer company containing credentials
     * @return response from SUNAT
     * @throws Exception if transmission fails
     */
    public SunatResponse sendInvoice(String signedXml, com.facthub.billing.company.domain.model.Company company) throws Exception {
        // 1. Configure URLs dynamically from application.properties (or default to Beta)
        CompanyURLs companyURLs = CompanyURLs.builder()
                .invoice(sunatUrl)
                .build();

        // 2. Configure Dynamic Company Credentials
        CompanyCredentials credentials = CompanyCredentials.builder()
                .username(company.getSunatSolUsername())
                .password(company.getSunatSolPassword())
                .build();

        // 3. Analyze XML and get data for sending
        byte[] xmlBytes = signedXml.getBytes(StandardCharsets.UTF_8);
        BillServiceXMLFileAnalyzer fileAnalyzer = new BillServiceXMLFileAnalyzer(xmlBytes, companyURLs);
        
        var zipFile = fileAnalyzer.getZipFile();
        var fileDestination = fileAnalyzer.getSendFileDestination();

        // 4. Build Camel data
        var camelData = CamelUtils.getBillServiceCamelData(zipFile, fileDestination, credentials);

        // 5. Send to SUNAT using CamelContext
        return camelContext.createProducerTemplate()
                .requestBodyAndHeaders(
                        Constants.XSENDER_BILL_SERVICE_URI,
                        camelData.getBody(),
                        camelData.getHeaders(),
                        SunatResponse.class
                );
    }
}
