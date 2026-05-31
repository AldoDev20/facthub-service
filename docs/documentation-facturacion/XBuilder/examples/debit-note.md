# DebitNote (Nota de Débito)

> Documento electrónico que aumenta el importe de una factura o boleta de venta.

---

## 📋 Descripción

El tipo `DebitNote` se utiliza para generar **Notas de Débito**, documentos que incrementan el monto de un comprobante de pago previamente emitido.

---

## 💻 Código de Ejemplo

### Crear DebitNote

```java
Defaults defaults;
DateProvider dateProvider;

DebitNote input = DebitNote.builder()
    .serie("FD01")
    .numero(1)
    .comprobanteAfectadoSerieNumero("F001-1")
    .sustentoDescripcion("mi sustento")
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
        .build()
    )
    .detalle(DocumentoVentaDetalle.builder()
        .descripcion("Item2")
        .cantidad(new BigDecimal("10"))
        .precio(new BigDecimal("100"))
        .build()
    )
    .build();

ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
enricher.enrich(input);

Template template = TemplateProducer.getInstance().getDebitNote();
String xml = template.data(input).render();
```

---

## 📝 Campos Principales

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| `serie` | String | Serie de la nota de débito | `"FD01"` |
| `numero` | int | Número de la nota de débito | `1` |
| `comprobanteAfectadoSerieNumero` | String | Serie-número del comprobante afectado | `"F001-1"` |
| `sustentoDescripcion` | String | Motivo o sustento de la nota de débito | `"mi sustento"` |
| `proveedor` | Proveedor | Datos del emisor | RUC y razón social |
| `cliente` | Cliente | Datos del receptor | Nombre y documento |
| `detalle` | DocumentoVentaDetalle | Items de la nota de débito | Descripción, cantidad, precio |

---

## 🔗 Ver También

- [Documentación Principal](../xbuilder.md)
- [Factura/Boleta](invoice-bolata-factura.md)
- [Nota de Crédito](credit-note.md)
