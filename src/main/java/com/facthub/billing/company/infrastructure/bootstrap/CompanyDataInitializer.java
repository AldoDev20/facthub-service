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
        // Define the 4 fixed Workshops for the demonstration
        var demoWorkshops = java.util.List.of(
            new String[]{"20551234567", "TALLER MULTIMARCAS E.I.R.L.", "certificado-prueba.pfx"},
            new String[]{"20601234568", "MOTORES DEL NORTE S.A.", "certificado-prueba.pfx"},
            new String[]{"20543216789", "TECNOLOGIA Y MOTORES PERU S.A.C.", "certificado-prueba.pfx"},
            new String[]{"20556677889", "Taller Atelier SAC", "certificado-atelier.pfx"}
        );

        for (String[] workshop : demoWorkshops) {
            String ruc = workshop[0];
            String name = workshop[1];
            String certFileName = workshop[2];

            if (companyRepository.findByRuc(ruc).isEmpty()) {
                System.out.println("Initializing Test Company in DB: " + name);

                // Load the specific certificate for this company from resources
                ClassPathResource certResource = new ClassPathResource(certFileName);
                byte[] certBytes;
                try (InputStream is = certResource.getInputStream()) {
                    certBytes = is.readAllBytes();
                }

                Company testCompany = Company.builder()
                        .ruc(ruc)
                        .businessName(name)
                        .sunatSolUsername("12345678959MODDATOS")
                        .sunatSolPassword("MODDATOS")
                        .certificateContent(certBytes)
                        .certificatePassword("miclave")
                        .build();

                // Override UUID if it's the specific company from the request
                if ("20556677889".equals(ruc)) {
                    testCompany.setId(java.util.UUID.fromString("ef0a60ce-30b9-4d32-94d8-ae340c2d36fb"));
                }

                companyRepository.save(testCompany);
                System.out.println("Workshop registered successfully: " + ruc);
            }
        }
    }
}
