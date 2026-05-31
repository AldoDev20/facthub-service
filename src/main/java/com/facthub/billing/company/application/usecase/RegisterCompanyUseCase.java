package com.facthub.billing.company.application.usecase;

import com.facthub.billing.company.domain.model.Company;
import com.facthub.billing.company.domain.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class RegisterCompanyUseCase {

    private final CompanyRepository companyRepository;

    public RegisterCompanyUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Registra una nueva empresa en la base de datos junto con su certificado digital.
     * Si la empresa ya existe, actualiza sus datos y su certificado.
     *
     * @param ruc RUC de la empresa
     * @param businessName Razón Social
     * @param sunatSolUsername Usuario SOL
     * @param sunatSolPassword Clave SOL
     * @param certificatePassword Clave del certificado .pfx
     * @param certificateFile Archivo .pfx enviado por multipart
     * @return La empresa registrada
     */
    @Transactional
    public Company execute(
            String ruc,
            String businessName,
            String sunatSolUsername,
            String sunatSolPassword,
            String certificatePassword,
            MultipartFile certificateFile) {

        if (certificateFile == null || certificateFile.isEmpty()) {
            throw new IllegalArgumentException("El archivo del certificado digital (.pfx) es obligatorio.");
        }

        byte[] certificateBytes;
        try {
            certificateBytes = certificateFile.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de certificado: " + e.getMessage(), e);
        }

        Optional<Company> existingCompanyOpt = companyRepository.findByRuc(ruc);

        Company company;
        if (existingCompanyOpt.isPresent()) {
            company = existingCompanyOpt.get();
            company.setBusinessName(businessName);
            company.setSunatSolUsername(sunatSolUsername);
            company.setSunatSolPassword(sunatSolPassword);
            company.setCertificateContent(certificateBytes);
            company.setCertificatePassword(certificatePassword);
        } else {
            company = Company.builder()
                    .ruc(ruc)
                    .businessName(businessName)
                    .sunatSolUsername(sunatSolUsername)
                    .sunatSolPassword(sunatSolPassword)
                    .certificateContent(certificateBytes)
                    .certificatePassword(certificatePassword)
                    .build();
        }

        return companyRepository.save(company);
    }
}
