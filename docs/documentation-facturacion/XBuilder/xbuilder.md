# XBuilder - Documentación Principal

## 📖 Introducción

**XBuilder** es una librería Java que crea y firma comprobantes electrónicos en formato UBL (XML).

### 📚 Recursos

- [Maven Central](https://mvnrepository.com/artifact/io.github.project-openubl/xbuilder)
- [Versiones Anteriores](#) - Para ver documentación de versiones anteriores visite: [Previous versions](#)

---

## 📄 Documentos Soportados

| # | Documento | Tipo UBL |
|---|-----------|----------|
| 1 | Boleta | `Invoice` |
| 2 | Factura | `Invoice` |
| 3 | Nota de Crédito | `CreditNote` |
| 4 | Nota de Débito | `DebitNote` |
| 5 | Baja | `VoidedDocuments` |
| 6 | Resumen Diario | `SummaryDocuments` |
| 7 | Percepción | `Perception` |
| 8 | Retención | `Retention` |
| 9 | Guía de Remisión | `DespatchDocument` |

---

## 📦 Instalación

XBuilder puede ser usado y descargado desde el repositorio central de Maven. Las versiones pueden ser consultadas en:

- [Maven Central](https://mvnrepository.com/artifact/io.github.project-openubl/xbuilder)

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

## 🛠️ Crear XML

### Paso 1: Crea el comprobante

```java
// Given
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

### Paso 2: Ejecuta los cálculos automáticos

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

### Paso 3: Crea el XML

```java
Template template = TemplateProducer.getInstance().getInvoice();
String xml = template.data(input).render();
```

---

## 📖 Leer XML

### XML como String

```java
// Given
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

## ✍️ Firmar XML

Para firmar un XML necesitas tener dos objetos:

1. **X509Certificate**
2. **PrivateKey**

Puedes cargar esos objetos con los métodos y código que creas conveniente.

### Cargar certificado

#### Usar archivo .pfx

Puedes cargar `X509Certificate` y `PrivateKey` desde un archivo `.pfx` usando:

```java
InputStream ksInputStream = new FileInputStream(new File("myCertificate.pfx"));
CertificateDetails certificate = CertificateDetailsFactory.create(ksInputStream, "myCertificatePassword");

X509Certificate certificate = certificate.getX509Certificate();
PrivateKey privateKey = certificate.getPrivateKey();
```

### Firmar XML

```java
String xml; // Ver docs para crear un XML
String signatureID = "mySignID"; // Tu ID de firma

// Obtén tu certificado usando el método de tu preferencia
X509Certificate certificate;
PrivateKey privateKey;

Document signedXML = XMLSigner.signXML(xml, signatureID, certificate, privateKey);
```

> ✅ **¡Eso es todo, ya tienes tu XML firmado!**

---

### 💡 Tips Importantes

#### Tip 1: Tipo de objeto firmado

`signedXML` es un objeto de la clase `org.w3c.dom.Document`, por lo tanto **no debes usar** `System.out.println` para ver su contenido.

#### Tip 2: SignatureID

`signatureID` es el valor de `ds:Signature Id="mySignID"` dentro del XML, por ejemplo:

```xml
<ext:UBLExtensions>
    <ext:UBLExtension>
        <ext:ExtensionContent>
            <ds:Signature Id="mySignID" xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
            </ds:Signature>
        </ext:ExtensionContent>
    </ext:UBLExtension>
</ext:UBLExtensions>
```

---

## ❓ Preguntas Frecuentes

### ¿Qué es el SignatureID?

El `signatureID` es el valor de `ds:Signature Id="mySignID"` dentro del XML firmado:

```xml
<ext:UBLExtensions>
    <ext:UBLExtension>
        <ext:ExtensionContent>
            <ds:Signature Id="mySignID" xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
            </ds:Signature>
        </ext:ExtensionContent>
    </ext:UBLExtension>
</ext:UBLExtensions>
```

#### 🎯 Selección de un buen SignatureID

| ❌ Incorrecto | ✅ Correcto |
|--------------|------------|
| `12345678912` | `MiEmpresa` |
| Contiene solo números | Contiene letras |
| Contiene espacios | Sin espacios |

> ⚠️ **La SUNAT rechaza** signatureIDs como `12345678912` pero **sí acepta** valores como `MiEmpresa`.

> 🚨 **Importante:** En caso de definir signatureIDs inválidos, probablemente tendrás el error **"No se puede leer (parsear) el archivo XML"** al momento de enviar el XML a la SUNAT.

---

### ¿Cómo ver el contenido del XML firmado?

El XML firmado es obtenido en un objeto de la forma:

```java
Document signedXML = XMLSigner.signXML(xml, signatureID, certificate, privateKey);
```

> 📌 **Nota:** El XML firmado **no es un String** sino un `org.w3c.dom.Document`, por lo tanto **no debes** intentar imprimirlo usando `System.out.println(signedXML)`.

Lo que debes hacer es escribirlo en un disco duro o convertirlo a `bytes[]` para que puedas empezar a usarlo.

#### Ejemplo: Guardar en archivo

```java
DOMSource source = new DOMSource(signedDocument);
FileWriter writer = new FileWriter(new File("D:/sunat/operaciones/12345678959-01-F001-00000001.xml"));
StreamResult resultXml = new StreamResult(writer);

TransformerFactory transformerFactory = TransformerFactory.newInstance();
Transformer transformer = transformerFactory.newTransformer();
transformer.transform(source, resultXml);

File file = new File("D:/sunat/operaciones/12345678959-01-F001-00000001.xml"); // Este archivo contiene el XML firmado
```
