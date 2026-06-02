---
name: facthub-security-and-testing-patterns
description: Security patterns for credentials and ArchUnit testing configuration for FactHub billing service
source: auto-skill
extracted_at: '2026-05-31T16:35:07.557Z'
---

# FactHub Security and Testing Patterns

## Database Credential Security

**Problem:** GitHub detects and blocks commits containing database credentials in `application.properties`.

**Solution:** 
1. Add `src/main/resources/application.properties` to `.gitignore`
2. Create `application.properties.example` as a template with placeholder values
3. Keep real credentials only in local `application.properties`

**.gitignore entry:**
```
### Secrets ###
src/main/resources/application.properties
```

**Template file:** `src/main/resources/application.properties.example`
```properties
spring.datasource.password=<TU_PASSWORD_AQUI>
```

## ArchUnit Configuration for Empty Packages

**Problem:** ArchUnit tests fail when checking rules on empty packages (no classes to validate).

**Solution:** 
- Comment out ArchUnit rules until the corresponding classes exist
- Enable rules incrementally as classes are added to the project
- Use TODO comments to track which rules need activation

**Example pattern:**
```java
// TODO: Enable when controller classes are added
// @ArchTest
// static final ArchRule controllers_must_end_with_Controller =
//         classes().that().resideInAPackage("..controller..")
//                 .should().haveSimpleNameEndingWith("Controller");
```

## Hibernate DDL Strategy

**Development:** `spring.jpa.hibernate.ddl-auto=update` (allows schema evolution)
**Production:** `spring.jpa.hibernate.ddl-auto=validate` (prevents accidental schema changes)

**Note:** The database already has `invoice` and `invoice_sequence` tables created, so `validate` is appropriate for production.
