---
name: facthub-phased-implementation-workflow
description: Phased development workflow with feature branches, conventional commits, and iterative validation for FactHub billing service
source: auto-skill
extracted_at: '2026-05-31T17:19:43.427Z'
---

# FactHub Phased Implementation Workflow

## Context
The FactHub billing service is developed incrementally using a phased approach where each phase is a separate feature branch with one or more conventional commits. After each phase, development stops for user review and git operations before proceeding.

## Branch Strategy

All feature branches start from `develop`:

```
develop
  ├─► feature/setup-pom-dependencies
  ├─► feature/project-structure-ddd
  ├─► feature/setup-archunit
  ├─► feature/domain-entities
  ├─► feature/infra-postgresql-persistence
  ├─► feature/infra-searchpe-client
  ├─► feature/infra-sunat-xsender
  ├─► feature/app-billing-usecase
  ├─► feature/presentation-rest-api
  └─► release/v1.0.0
```

## Phase Execution Pattern

1. **Agent creates the branch** and implements all code for that phase
2. **Agent verifies** build compiles and tests pass
3. **Agent stops** and provides:
   - Exact git commands to execute
   - Conventional commit message
   - Instructions to merge to `develop`
4. **User executes** git commands and confirms
5. **Agent proceeds** to next phase

## Conventional Commit Messages by Phase

| Phase | Branch | Commit Message |
|-------|--------|----------------|
| Dependencies | `feature/setup-pom-dependencies` | `chore(deps): add xbuilder, xsender and archunit dependencies` |
| Structure | `feature/project-structure-ddd` | `feat(structure): create DDD package structure with billing, transmission, directory contexts and ComponentScan` |
| ArchUnit | `feature/setup-archunit` | `test(arch): add ArchUnit architecture test skeleton with database configuration` |
| Entities | `feature/domain-entities` | `feat(domain): add invoice entities, DTOs, and exception handling with JPA mappings` |
| Persistence | `feature/infra-postgresql-persistence` | `feat(persistence): add Spring Data JPA repositories for invoice and sequence management` |
| Searchpe | `feature/infra-searchpe-client` | `feat(infrastructure): add Searchpe HTTP client for taxpayer RUC validation` |
| XSender | `feature/infra-sunat-xsender` | `feat(infrastructure): add XSender SUNAT service skeleton with certificate and transmission models` |
| Billing | `feature/app-billing-usecase` | `feat(application): implement GenerateInvoiceUseCase with XBuilder XML generation and signing` |
| REST API | `feature/presentation-rest-api` | `feat(controller): add FacturacionController with POST /api/factura/emitir endpoint` |

## Credential Management

**Critical:** Database credentials must never be committed to git:

```gitignore
### Secrets ###
src/main/resources/application.properties
src/main/resources/*.pfx
```

- Keep real credentials in local `application.properties`
- Use `application.properties.example` as template
- Agent must update password from placeholder to real value before running tests
- User may replace with their own credentials

## Validation Checklist Per Phase

Before providing commit instructions:

- [ ] `mvnw.cmd clean compile` succeeds
- [ ] `mvnw.cmd test` passes (Tests run: X, Failures: 0)
- [ ] All new files follow DDD package structure
- [ ] No credentials in tracked files
- [ ] Commit message follows conventional commits format

## XSender Integration Note

The `spring-boot-xsender` library has complex internal package structure. When implementing XSender services:

1. Start with skeleton/placeholder implementation
2. Verify compilation without XSender imports first
3. Add actual XSender imports in later phase when XML generation is complete
4. Use `Object` return types temporarily to avoid import errors

## Test Certificate Generation

Generate self-signed certificate for local testing:

```powershell
$cert = New-SelfSignedCertificate -Subject 'CN=Mi Empresa, OU=IT, O=Mi Empresa, C=PE' `
  -CertStoreLocation Cert:\CurrentUser\My `
  -KeyExportPolicy Exportable `
  -KeySpec Signature

$pwd = ConvertTo-SecureString -String 'miclave' -Force -AsPlainText

Export-PfxCertificate -Cert $cert `
  -FilePath src\main\resources\certificado-prueba.pfx `
  -Password $pwd
```

- Password: `miclave`
- Add `*.pfx` to `.gitignore`
- Certificate required for XML signing in billing use case
