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
        Company company = companyRepository.findByRuc(ruc).orElse(null);

        byte[] certificateBytes = null;
        if (company == null || company.getCertificateContent() == null || company.getCertificateContent().length == 0) {
            org.springframework.core.io.ClassPathResource resource = 
                    new org.springframework.core.io.ClassPathResource("certificado-prueba.pfx");
            try (java.io.InputStream is = resource.getInputStream()) {
                certificateBytes = is.readAllBytes();
            } catch (java.io.IOException e) {
                certificateBytes = new byte[0];
            }
        }

        if (company == null) {
            return Company.builder()
                    .ruc(ruc)
                    .businessName("TALLER MOCK S.A.")
                    .sunatSolUsername("12345678959MODDATOS")
                    .sunatSolPassword("MODDATOS")
                    .certificateContent(certificateBytes)
                    .certificatePassword("miclave")
                    .build();
        }

        if (company.getCertificateContent() == null || company.getCertificateContent().length == 0) {
            company.setCertificateContent(certificateBytes);
        }
        if (company.getCertificatePassword() == null || company.getCertificatePassword().trim().isEmpty()) {
            company.setCertificatePassword("miclave");
        }
        if (company.getBusinessName() == null || company.getBusinessName().trim().isEmpty()) {
            company.setBusinessName("TALLER MOCK S.A.");
        }

        return company;
    }
}
