# XBuilder - Documentación Completa

## 📋 Índice

- [Introducción](#introducción)
- [Documentos Soportados](#documentos-soportados)
- [Instalación](#instalación)
- [Guía de Uso](#guía-de-uso)
  - [Crear XML](#crear-xml)
  - [Leer XML](#leer-xml)
  - [Firmar XML](#firmar-xml)
- [Ejemplos por Tipo de Documento](#ejemplos-por-tipo-de-documento)
  - [Factura/Boleta (Invoice)](#facturaboleta-invoice)
  - [Nota de Crédito (CreditNote)](#nota-de-crédito-creditnote)
  - [Nota de Débito (DebitNote)](#nota-de-débito-debitnote)
  - [Percepción (Perception)](#percepción-perception)
  - [Retención (Retention)](#retención-retention)
  - [Resumen Diario (SummaryDocument)](#resumen-diario-summarydocument)
  - [Baja (VoidedDocument)](#baja-voideddocument)
- [Preguntas Frecuentes](#preguntas-frecuentes)

---

## 📖 Introducción

**XBuilder** es una librería Java que crea y firma comprobantes electrónicos en formato UBL (XML).

### Repositorios

- [Maven Central](https://mvnrepository.com/artifact/io.github.project-openubl/xbuilder)
- [Versiones Anteriores](#)

---

## 📄 Documentos Soportados

| Documento | Tipo |
|-----------|------|
| Boleta | `Invoice` |
| Factura | `Invoice` |
| Nota de Crédito | `CreditNote` |
| Nota de Débito | `DebitNote` |
| Baja | `VoidedDocuments` |
| Resumen Diario | `SummaryDocuments` |
| Percepción | `Perception` |
| Retención | `Retention` |
| Guía de Remisión | `DespatchDocument` |

---

## 📦 Instalación

XBuilder puede ser usado y descargado desde el repositorio central de Maven. Las versiones pueden ser consultadas en [Maven Central](https://mvnrepository.com/artifact/io.github.project-openubl/xbuilder).

### Maven

Si usas Maven, en tu archivo `pom.xml` agrega:

```xml
<dependency>
    <groupId>io.github.project-openubl</groupId>
    <artifactId>xbuilder</artifactId>
    <version>VERSION</version>
</dependency>
```

---

## 🛠️ Guía de Uso

### Crear XML

#### 1. Crea el comprobante

```java
Invoice input = Invoice
    .builder()
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
```

#### 2. Ejecuta los cálculos automáticos

**Configura los valores globales por defecto:**

```java
Defaults defaults = Defaults.builder()
    .icbTasa(new BigDecimal("0.2"))
    .igvTasa(new BigDecimal("0.18"))
    .build();
```

**Configura el reloj del sistema:**

```java
DateProvider dateProvider = () -> LocalDate.of(2019, 12, 24);
```

**Ejecuta los cálculos automáticos:**

```java
ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
enricher.enrich(input);
```

#### 3. Crea el XML

```java
Template template = TemplateProducer.getInstance().getInvoice();
String xml = template.data(input).render();
```

---

### Leer XML

#### XML como String

```java
String xml;
javax.xml.bind.Unmarshaller unmarshaller; // Ejemplo: JAXBContext.newInstance(XMLInvoice.class).createUnmarshaller()

InvoiceMapper invoiceMapper = Mappers.getMapper(InvoiceMapper.class);

try (StringReader reader = new StringReader(xml)) {
    XMLInvoice pojo = (XMLInvoice) unmarshaller.unmarshal(new InputSource(reader));
    Invoice invoice = invoiceMapper.map(pojo);

    System.out.println("Mi POJO es: " + invoice.toString());
}
```

---

### Firmar XML

Para firmar un XML necesitas tener dos objetos:

- `X509Certificate`
- `PrivateKey`

Puedes cargar esos objetos con los métodos y código que creas conveniente.

#### Cargar certificado desde .pfx

```java
InputStream ksInputStream = new FileInputStream(new File("myCertificate.pfx"));
CertificateDetails certificate = CertificateDetailsFactory.create(ksInputStream, "myCertificatePassword");

X509Certificate certificate = certificate.getX509Certificate();
PrivateKey privateKey = certificate.getPrivateKey();
```

#### Firmar XML

```java
String xml; // Ver docs para crear un XML
String signatureID = "mySignID"; // Tu ID de firma

// Obtén tu certificado usando el método de tu preferencia
X509Certificate certificate;
PrivateKey privateKey;

Document signedXML = XMLSigner.signXML(xml, signatureID, certificate, privateKey);
```

> **💡 Tip:** `signedXML` es un objeto de la clase `org.w3c.dom.Document`, por lo tanto **no debes usar** `System.out.println` para ver su contenido.

> **💡 Tip:** `signatureID` es el valor de `ds:Signature Id="mySignID"` dentro del XML, por ejemplo:
>
> ```xml
> <ext:UBLExtensions>
>     <ext:UBLExtension>
>         <ext:ExtensionContent>
>             <ds:Signature Id="mySignID" xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
>             </ds:Signature>
>         </ext:ExtensionContent>
>     </ext:UBLExtension>
> </ext:UBLExtensions>
> ```

---

## 📝 Ejemplos por Tipo de Documento

### Factura/Boleta (Invoice)

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

### Nota de Crédito (CreditNote)

```java
Defaults defaults;
DateProvider dateProvider;

CreditNote input = CreditNote.builder()
    .serie("FC01")
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

Template template = TemplateProducer.getInstance().getCreditNote();
String xml = template.data(input).render();
```

---

### Nota de Débito (DebitNote)

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

### Percepción (Perception)

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

### Retención (Retention)

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

### Resumen Diario (SummaryDocument)

```java
Defaults defaults;
DateProvider dateProvider;

SummaryDocuments input = SummaryDocuments.builder()
    .numero(1)
    .fechaEmisionComprobantes(dateProvider.now().minusDays(2))
    .proveedor(Proveedor.builder()
        .ruc("12345678912")
        .razonSocial("Softgreen S.A.C.")
        .build()
    )
    .comprobante(SummaryDocumentsItem.builder()
        .tipoOperacion(Catalog19.ADICIONAR.toString())
        .comprobante(Comprobante.builder()
            .tipoComprobante(Catalog1_Invoice.BOLETA.getCode())
            .serieNumero("B001-1")
            .cliente(Cliente.builder()
                .nombre("Carlos Feria")
                .numeroDocumentoIdentidad("12345678")
                .tipoDocumentoIdentidad(Catalog6.DNI.getCode())
                .build()
            )
            .impuestos(ComprobanteImpuestos.builder()
                .igv(new BigDecimal("18"))
                .icb(new BigDecimal(2))
                .build()
            )
            .valorVenta(ComprobanteValorVenta.builder()
                .importeTotal(new BigDecimal("120"))
                .gravado(new BigDecimal("120"))
                .build()
            )
            .build()
        )
        .build()
    )
    .comprobante(SummaryDocumentsItem.builder()
        .tipoOperacion(Catalog19.ADICIONAR.toString())
        .comprobante(Comprobante.builder()
            .tipoComprobante(Catalog1.NOTA_CREDITO.getCode())
            .serieNumero("BC02-2")
            .comprobanteAfectado(ComprobanteAfectado.builder()
                .serieNumero("B002-2")
                .tipoComprobante(Catalog1.BOLETA.getCode())
                .build()
            )
            .cliente(Cliente.builder()
                .nombre("Carlos Feria")
                .numeroDocumentoIdentidad("12345678")
                .tipoDocumentoIdentidad(Catalog6.DNI.getCode())
                .build()
            )
            .impuestos(ComprobanteImpuestos.builder()
                .igv(new BigDecimal("18"))
                .build()
            )
            .valorVenta(ComprobanteValorVenta.builder()
                .importeTotal(new BigDecimal("118"))
                .gravado(new BigDecimal("118"))
                .build()
            )
            .build()
        )
        .build()
    )
    .build();

ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
enricher.enrich(input);

Template template = TemplateProducer.getInstance().getSummaryDocuments();
String xml = template.data(input).render();
```

---

### Baja (VoidedDocument)

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

## ❓ Preguntas Frecuentes

### ¿Qué es el SignatureID?

El `signatureID` es el valor de `ds:Signature Id="mySignID"` dentro del XML firmado.

**Selecciona un buen signatureID:**

- ✅ **No debe** contener números ni espacios en blanco
- ✅ SUNAT rechaza signatureIDs como `12345678912`
- ✅ SUNAT acepta valores como `MiEmpresa`

> ⚠️ **Importante:** En caso de definir signatureIDs inválidos, probablemente tendrás el error *"No se puede leer (parsear) el archivo XML"* al momento de enviar el XML a la SUNAT.

### ¿Cómo ver el contenido del XML firmado?

El XML firmado es obtenido en un objeto de la forma:

```java
Document signedXML = XMLSigner.signXML(xml, signatureID, certificate, privateKey);
```

> **Nota:** El XML firmado **no es un String** sino un `org.w3c.dom.Document`, por lo tanto **no debes** intentar imprimirlo usando `System.out.println(signedXML)`.

Lo que debes hacer es escribirlo en un disco duro o convertirlo a `bytes[]` para que puedas empezar a usarlo.

**Ejemplo:**

```java
DOMSource source = new DOMSource(signedDocument);
FileWriter writer = new FileWriter(new File("D:/sunat/operaciones/12345678959-01-F001-00000001.xml"));
StreamResult resultXml = new StreamResult(writer);

TransformerFactory transformerFactory = TransformerFactory.newInstance();
Transformer transformer = transformerFactory.newTransformer();
transformer.transform(source, resultXml);

File file = new File("D:/sunat/operaciones/12345678959-01-F001-00000001.xml"); // Este archivo contiene el XML firmado
```

---

## 📌 Resumen de Patrones de Uso

Todos los documentos siguen el mismo patrón:

1. **Construir** el objeto con el Builder pattern
2. **Enriquecer** con `ContentEnricher`
3. **Renderizar** con `TemplateProducer`

```java
// Paso 1: Construir
TuDocumento input = TuDocumento.builder()
    // ... campos
    .build();

// Paso 2: Enriquecer
ContentEnricher enricher = new ContentEnricher(defaults, dateProvider);
enricher.enrich(input);

// Paso 3: Renderizar
Template template = TemplateProducer.getInstance().getTuDocumento();
String xml = template.data(input).render();
```
