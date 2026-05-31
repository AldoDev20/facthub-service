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
        String testRuc = "20123456789";
        
        // Verifica si la empresa de prueba ya existe
        if (companyRepository.findByRuc(testRuc).isEmpty()) {
            System.out.println("Inicializando Empresa de Prueba en la base de datos...");

            // Cargar el certificado de prueba de los recursos
            ClassPathResource certResource = new ClassPathResource("certificado-prueba.pfx");
            byte[] certBytes;
            try (InputStream is = certResource.getInputStream()) {
                certBytes = is.readAllBytes();
            }

            // Crear la empresa
            Company testCompany = Company.builder()
                    .ruc(testRuc)
                    .businessName("EMPRESA EMISORA DE PRUEBA S.A.C.")
                    .sunatSolUsername("12345678959MODDATOS")
                    .sunatSolPassword("MODDATOS")
                    .certificateContent(certBytes)
                    .certificatePassword("miclave")
                    .build();

            companyRepository.save(testCompany);
            
            System.out.println("Empresa de Prueba registrada exitosamente: " + testRuc);
        }
    }
}
