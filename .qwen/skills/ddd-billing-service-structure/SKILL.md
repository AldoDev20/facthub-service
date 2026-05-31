---
name: ddd-billing-service-structure
description: DDD package structure and implementation approach for Peruvian electronic invoicing microservice using Spring Boot, XBuilder, XSender, and Searchpe
source: auto-skill
extracted_at: '2026-05-31T16:13:36.190Z'
---

# DDD Billing Service Structure

## Context
This skill captures the architecture and implementation approach for the FactHub billing microservice, a Spring Boot application for Peruvian electronic invoicing (facturación electrónica) that integrates with SUNAT via XBuilder/XSender and validates taxpayer data via Searchpe API.

## DDD Package Structure

The service uses **Bounded Contexts** organized under `com.facthub.billing`:

```
com.facthub.billing
├── billing/              # Core Domain - Invoice generation & business rules
│   ├── domain/
│   │   ├── model/        # Invoice, InvoiceItem, InvoiceSequence entities
│   │   ├── repository/   # InvoiceRepository interface
│   │   └── exception/    # InvalidInvoiceException
│   ├── application/
│   │   ├── usecase/      # GenerateInvoiceUseCase
│   │   └── dto/          # FacturaRequestDto, ItemDto
│   └── infrastructure/
│       ├── persistence/  # JpaInvoiceRepository implementation
│       └── xbuilder/     # XBuilderFacturacionService (XML generation)
│
├── transmission/         # Supporting Domain - SUNAT communication
│   ├── domain/
│   │   ├── model/        # SunatTicket entity
│   │   └── repository/   # TransmissionLogRepository
│   ├── application/
│   │   └── usecase/      # SendInvoiceToSunatUseCase
│   └── infrastructure/
│       ├── sunat/        # XSenderSunatService (SOAP via Camel)
│       └── config/       # CamelConfig
│
├── directory/            # Supporting Domain - Taxpayer validation
│   ├── domain/
│   │   └── model/        # Taxpayer entity
│   ├── application/
│   │   └── usecase/      # GetTaxpayerInfoUseCase
│   └── infrastructure/
│       ├── searchpe/     # SearchpeClient (HTTP to searchpe-atelier.onrender.com)
│       └── dto/          # ContribuyenteDto
│
├── shared/               # Shared Kernel
│   └── exception/        # GlobalExceptionHandler (@ControllerAdvice)
│
└── presentation/         # REST API layer
    └── controller/       # FacturacionController
```

## Key Dependencies

```xml
<!-- XBuilder: XML UBL generation -->
<dependency>
    <groupId>io.github.project-openubl</groupId>
    <artifactId>xbuilder</artifactId>
    <version>5.0.2</version>
</dependency>

<!-- XSender: SUNAT SOAP delivery via Apache Camel -->
<dependency>
    <groupId>io.github.project-openubl</groupId>
    <artifactId>spring-boot-xsender</artifactId>
    <version>5.0.2</version>
</dependency>

<!-- ArchUnit: Architectural testing -->
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```

## Application Class Configuration

Must include `@ComponentScan` to initialize XSender/Camel components:

```java
@SpringBootApplication
@ComponentScan({"com.facthub.billing", "io.github.project.openubl.spring.xsender.runtime"})
public class FacthubServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FacthubServiceApplication.class, args);
    }
}
```

## ArchUnit Setup Notes

When packages are empty (no classes yet), ArchUnit rules will fail. **Solution**: Comment out rules until classes exist, or add rules progressively in later phases:

```java
// TODO: Enable when controller classes are added
// @ArchTest
// static final ArchRule controllers_must_end_with_Controller =
//         classes().that().resideInAPackage("..controller..")
//                 .should().haveSimpleNameEndingWith("Controller");
```

## External Integrations

| Service | URL | Purpose |
|---------|-----|---------|
| Searchpe | https://searchpe-atelier.onrender.com | Taxpayer RUC validation |
| SUNAT Beta | https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService | Invoice submission |
| PostgreSQL (Aiven) | facthub-facthub.d.aivencloud.com:13247 | Invoice persistence |

## Git Workflow

- Branch from `develop` for each feature
- Use **Conventional Commits** in English
- Merge back to `develop` after each phase
- Branch naming: `feature/<description>`, commits: `<type>(<scope>): <description>`
