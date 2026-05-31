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
        // Cargar el certificado de prueba de los recursos
        ClassPathResource certResource = new ClassPathResource("certificado-prueba.pfx");
        byte[] certBytes;
        try (InputStream is = certResource.getInputStream()) {
            certBytes = is.readAllBytes();
        }

        // Definir los 3 Talleres fijos para la demostración
        var talleresDemo = java.util.List.of(
            new String[]{"20551234567", "TALLER MULTIMARCAS E.I.R.L."},
            new String[]{"20601234568", "MOTORES DEL NORTE S.A."},
            new String[]{"20543216789", "TECNOLOGIA Y MOTORES PERU S.A.C."}
        );

        for (String[] taller : talleresDemo) {
            String ruc = taller[0];
            String nombre = taller[1];

            if (companyRepository.findByRuc(ruc).isEmpty()) {
                System.out.println("Inicializando Empresa de Prueba en la BD: " + nombre);

                Company testCompany = Company.builder()
                        .ruc(ruc)
                        .businessName(nombre)
                        .sunatSolUsername("12345678959MODDATOS")
                        .sunatSolPassword("MODDATOS")
                        .certificateContent(certBytes)
                        .certificatePassword("miclave")
                        .build();

                companyRepository.save(testCompany);
                System.out.println("Taller registrado exitosamente: " + ruc);
            }
        }
    }
}
