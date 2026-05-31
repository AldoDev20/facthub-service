# VoidedDocument (Baja)

> Documento electrónico que comunica la anulación de comprobantes de pago.

---

## 📋 Descripción

El tipo `VoidedDocuments` se utiliza para generar **Comunicaciones de Baja**, documentos que informan a SUNAT sobre la anulación de comprobantes de pago emitidos.

---

## 💻 Código de Ejemplo

### Crear VoidedDocument

```java
Defaults defaults;
DateProvider dateProvider;

VoidedDocuments input = VoidedDocuments.builder()
    .numero(1)
    .fechaEmision(LocalDate.of(2022, 01, 31))
    .fechaEmisionComprobantes(LocalDate.of(2022, 01, 29))
    .proveedor(Proveedor.builder()
        .ruc("12345678912")
        .razonSocial("Softgreen S.A.C.")
        .build()
    )
    .comprobante(VoidedDocumentsItem.builder()
        .serie("F001")
        .numero(1)
        .tipoComprobante(Catalog1_Invoice.FACTURA.getCode())
        .descripcionSustento("Mi sustento1")
        .build()
    )
    .comprobante(VoidedDocumentsItem.builder()
        .serie("F001")
        .numero(2)
        .tipoComprobante(Catalog1_Invoice.FACTURA.getCode())
        .descripcionSustento("Mi sustento2")
        .build()
    )
    .build();

ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
enricher.enrich(input);

Template template = TemplateProducer.getInstance().getVoidedDocument();
String xml = template.data(input).render();
```

---

## 📝 Campos Principales

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| `numero` | int | Número de la comunicación de baja | `1` |
| `fechaEmision` | LocalDate | Fecha de emisión del documento | `2022-01-31` |
| `fechaEmisionComprobantes` | LocalDate | Fecha de emisión de los comprobantes dados de baja | `2022-01-29` |
| `proveedor` | Proveedor | Datos del emisor | RUC y razón social |
| `comprobante` | VoidedDocumentsItem | Items de la baja | Lista de comprobantes anulados |

### Campos de VoidedDocumentsItem

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| `serie` | String | Serie del comprobante anulado | `"F001"` |
| `numero` | int | Número del comprobante anulado | `1` |
| `tipoComprobante` | String | Tipo de comprobante | `Catalog1_Invoice.FACTURA` |
| `descripcionSustento` | String | Motivo de la anulación | `"Mi sustento1"` |

---

## 🔗 Ver También

- [Documentación Principal](../xbuilder.md)
- [Resumen Diario](sumary-document.md)
