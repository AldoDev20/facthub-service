# Perception (Percepción)

> Documento electrónico que registra la percepción del IGV en operaciones de venta.

---

## 📋 Descripción

El tipo `Perception` se utiliza para generar **Comprobantes de Percepción**, documentos que acreditan el cobro anticipado del Impuesto General a las Ventas (IGV).

---

## 💻 Código de Ejemplo

### Crear Perception

```java
Defaults defaults;
DateProvider dateProvider;

Perception input = Perception.builder()
    .serie("P001")
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
    .importeTotalPercibido(new BigDecimal("10"))
    .importeTotalCobrado(new BigDecimal("210"))
    .tipoRegimen(Catalog22.VENTA_INTERNA.getCode())
    .tipoRegimenPorcentaje(Catalog22.VENTA_INTERNA.getPercent())
    .operacion(PercepcionRetencionOperacion.builder()
        .numeroOperacion(1)
        .fechaOperacion(LocalDate.of(2022, 01, 31))
        .importeOperacion(new BigDecimal("100"))
        .comprobante(ComprobanteAfectado.builder()
            .tipoComprobante(Catalog1.FACTURA.getCode())
            .serieNumero("F001-1")
            .fechaEmision(LocalDate.of(2022, 01, 31))
            .importeTotal(new BigDecimal("200"))
            .moneda("PEN")
            .build()
        )
        .build()
    )
    .build();

ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
enricher.enrich(input);

Template template = TemplateProducer.getInstance().getPerception();
String xml = template.data(input).render();
```

---

## 📝 Campos Principales

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| `serie` | String | Serie del comprobante de percepción | `"P001"` |
| `numero` | int | Número del comprobante | `1` |
| `fechaEmision` | LocalDate | Fecha de emisión | `2022-01-31` |
| `proveedor` | Proveedor | Datos del agente de percepción | RUC y razón social |
| `cliente` | Cliente | Datos del sujeto perceptido | Nombre y documento |
| `importeTotalPercibido` | BigDecimal | Total del importe percibido | `10` |
| `importeTotalCobrado` | BigDecimal | Total del importe cobrado | `210` |
| `tipoRegimen` | String | Código del régimen de percepción | `Catalog22.VENTA_INTERNA` |
| `tipoRegimenPorcentaje` | BigDecimal | Porcentaje del régimen | `Catalog22.VENTA_INTERNA.getPercent()` |
| `operacion` | PercepcionRetencionOperacion | Detalle de la operación | Datos del comprobante afectado |

---

## 🔗 Ver También

- [Documentación Principal](../xbuilder.md)
- [Retención](retention.md)
