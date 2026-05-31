# Invoice (Boleta/Factura)

> Documento electrónico de venta según normativa SUNAT.

---

## 📋 Descripción

El tipo `Invoice` se utiliza para generar:

- **Facturas** (serie F*)
- **Boletas de Venta** (serie B*)

---

## 💻 Código de Ejemplo

### Crear Invoice

```java
Defaults defaults;
DateProvider dateProvider;

Invoice input = Invoice.builder()
    .serie("F001")
    .numero(1)
    .proveedor(Proveedor.builder()
        .ruc("12345678912")
        .razonSocial("Softgreen S.A.C.")
        .build()
    )
    .cliente(Cliente.builder()
        .nombre("Carlos Feria")
        .numeroDocumentoIdentidad("12121212121")
        .tipoDocumentoIdentidad(Catalog6.RUC.toString())
        .build()
    )
    .detalle(DocumentoVentaDetalle.builder()
        .descripcion("Item1")
        .cantidad(new BigDecimal("10"))
        .precio(new BigDecimal("100"))
        .unidadMedida("KGM")
        .build()
    )
    .detalle(DocumentoVentaDetalle.builder()
        .descripcion("Item2")
        .cantidad(new BigDecimal("10"))
        .precio(new BigDecimal("100"))
        .unidadMedida("KGM")
        .build()
    )
    .build();

ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
enricher.enrich(input);

Template template = TemplateProducer.getInstance().getInvoice();
String xml = template.data(input).render();
```

---

## 📝 Campos Principales

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| `serie` | String | Serie del comprobante | `"F001"` |
| `numero` | int | Número del comprobante | `1` |
| `proveedor` | Proveedor | Datos del emisor | RUC y razón social |
| `cliente` | Cliente | Datos del receptor | Nombre y documento |
| `detalle` | DocumentoVentaDetalle | Items del comprobante | Descripción, cantidad, precio |

---

## 🔗 Ver También

- [Documentación Principal](../xbuilder.md)
- [Nota de Crédito](credit-note.md)
- [Nota de Débito](debit-note.md)
