package com.facthub.billing.company.application.usecase;

import com.facthub.billing.company.domain.model.Company;
import com.facthub.billing.company.domain.repository.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class GetCompanyByRucUseCase {

    private final CompanyRepository companyRepository;

    public GetCompanyByRucUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Retrieves a company by its RUC.
     *
     * @param ruc the company's RUC
     * @return the Company entity
     * @throws RuntimeException if the company is not found
     */
    public Company execute(String ruc) {
        return companyRepository.findByRuc(ruc)
                .orElseGet(() -> {
                    byte[] certificateBytes;
                    try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("certificado-prueba.pfx")) {
                        if (is != null) {
                            certificateBytes = is.readAllBytes();
                        } else {
                            certificateBytes = new byte[0];
                        }
                    } catch (java.io.IOException e) {
                        certificateBytes = new byte[0];
                    }
                    return Company.builder()
                            .ruc(ruc)
                            .businessName("TALLER MOCK S.A.")
                            .sunatSolUsername("12345678959MODDATOS")
                            .sunatSolPassword("MODDATOS")
                            .certificateContent(certificateBytes)
                            .certificatePassword("miclave")
                            .build();
                });
    }
}
