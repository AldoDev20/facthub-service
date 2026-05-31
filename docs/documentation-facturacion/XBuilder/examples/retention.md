# Retention (Retención)

> Documento electrónico que registra la retención del IGV en operaciones de compra.

---

## 📋 Descripción

El tipo `Retention` se utiliza para generar **Comprobantes de Retención**, documentos que acreditan la deducción del Impuesto General a las Ventas (IGV) en operaciones de compra.

---

## 💻 Código de Ejemplo

### Crear Retention

```java
Defaults defaults;
DateProvider dateProvider;

Retention input = Retention.builder()
    .serie("R001")
    .numero(1)
    .fechaEmision(LocalDate.of(2022, 01, 31))
    .proveedor(Proveedor.builder()
        .ruc("12345678912")
        .razonSocial("Softgreen S.A.C.")
        .build()
    )
    .cliente(Cliente.builder()
        .nombre("Carlos Feria")
        .numeroDocumentoIdentidad("12121212121")
        .tipoDocumentoIdentidad(Catalog6.RUC.getCode())
        .build()
    )
    .importeTotalRetenido(new BigDecimal("10"))
    .importeTotalPagado(new BigDecimal("200"))
    .tipoRegimen(Catalog23.TASA_TRES.getCode())
    .tipoRegimenPorcentaje(Catalog23.TASA_TRES.getPercent())
    .operacion(PercepcionRetencionOperacion.builder()
        .numeroOperacion(1)
        .fechaOperacion(LocalDate.of(2022, 01, 31))
        .importeOperacion(new BigDecimal("100"))
        .comprobante(ComprobanteAfectado.builder()
            .tipoComprobante(Catalog1.FACTURA.getCode())
            .serieNumero("F001-1")
            .fechaEmision(LocalDate.of(2022, 01, 31))
            .importeTotal(new BigDecimal("210"))
            .moneda("PEN")
            .build()
        )
        .build()
    )
    .build();

ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
enricher.enrich(input);

Template template = TemplateProducer.getInstance().getRetention();
String xml = template.data(input).render();
```

---

## 📝 Campos Principales

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| `serie` | String | Serie del comprobante de retención | `"R001"` |
| `numero` | int | Número del comprobante | `1` |
| `fechaEmision` | LocalDate | Fecha de emisión | `2022-01-31` |
| `proveedor` | Proveedor | Datos del agente de retención | RUC y razón social |
| `cliente` | Cliente | Datos del sujeto retenido | Nombre y documento |
| `importeTotalRetenido` | BigDecimal | Total del importe retenido | `10` |
| `importeTotalPagado` | BigDecimal | Total del importe pagado | `200` |
| `tipoRegimen` | String | Código del régimen de retención | `Catalog23.TASA_TRES` |
| `tipoRegimenPorcentaje` | BigDecimal | Porcentaje del régimen | `Catalog23.TASA_TRES.getPercent()` |
| `operacion` | PercepcionRetencionOperacion | Detalle de la operación | Datos del comprobante afectado |

---

## 🔗 Ver También

- [Documentación Principal](../xbuilder.md)
- [Percepción](perception.md)
