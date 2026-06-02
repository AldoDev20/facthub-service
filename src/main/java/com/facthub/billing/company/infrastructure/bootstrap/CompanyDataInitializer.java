package com.facthub.billing.company.infrastructure.bootstrap;

import com.facthub.billing.company.domain.model.Company;
import com.facthub.billing.company.domain.repository.CompanyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class CompanyDataInitializer implements CommandLineRunner {

    private final CompanyRepository companyRepository;

    public CompanyDataInitializer(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Load the test certificate from resources
        ClassPathResource certResource = new ClassPathResource("certificado-prueba.pfx");
        byte[] certBytes;
        try (InputStream is = certResource.getInputStream()) {
            certBytes = is.readAllBytes();
        }

        // Define the 3 fixed Workshops for the demonstration
        var demoWorkshops = java.util.List.of(
            new String[]{"20551234567", "TALLER MULTIMARCAS E.I.R.L."},
            new String[]{"20601234568", "MOTORES DEL NORTE S.A."},
            new String[]{"20543216789", "TECNOLOGIA Y MOTORES PERU S.A.C."}
        );

        for (String[] workshop : demoWorkshops) {
            String ruc = workshop[0];
            String name = workshop[1];

            if (companyRepository.findByRuc(ruc).isEmpty()) {
                System.out.println("Initializing Test Company in DB: " + name);

                Company testCompany = Company.builder()
                        .ruc(ruc)
                        .businessName(name)
                        .sunatSolUsername("12345678959MODDATOS")
                        .sunatSolPassword("MODDATOS")
                        .certificateContent(certBytes)
                        .certificatePassword("miclave")
                        .build();

                companyRepository.save(testCompany);
                System.out.println("Workshop registered successfully: " + ruc);
            }
        }
    }
}
