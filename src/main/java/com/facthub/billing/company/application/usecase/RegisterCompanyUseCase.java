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
     * Registers a new company in the database along with its digital certificate.
     * If the company already exists, updates its data and certificate.
     *
     * @param ruc Company RUC
     * @param businessName Business Name
     * @param sunatSolUsername SOL Username
     * @param sunatSolPassword SOL Password
     * @param certificatePassword Password for the .pfx certificate
     * @param certificateFile The .pfx file sent via multipart
     * @return The registered company
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
            throw new IllegalArgumentException("The digital certificate file (.pfx) is required.");
        }

        // Clean up inputs (in case the user accidentally sends quotes or spaces in form-data)
        ruc = ruc != null ? ruc.replaceAll("\"", "").trim() : null;
        businessName = businessName != null ? businessName.replaceAll("\"", "").trim() : null;
        sunatSolUsername = sunatSolUsername != null ? sunatSolUsername.replaceAll("\"", "").trim() : null;
        sunatSolPassword = sunatSolPassword != null ? sunatSolPassword.replaceAll("\"", "").trim() : null;
        certificatePassword = certificatePassword != null ? certificatePassword.replaceAll("\"", "").trim() : null;

        if (ruc == null || ruc.length() != 11) {
            throw new IllegalArgumentException("El RUC debe tener exactamente 11 caracteres. RUC recibido: '" + ruc + "' (longitud: " + (ruc != null ? ruc.length() : "null") + ")");
        }

        byte[] certificateBytes;
        try {
            certificateBytes = certificateFile.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error reading the certificate file: " + e.getMessage(), e);
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
