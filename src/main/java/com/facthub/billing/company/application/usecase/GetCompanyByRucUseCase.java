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
                .orElseThrow(() -> new RuntimeException("Empresa emisora no encontrada con RUC: " + ruc));
    }
}
