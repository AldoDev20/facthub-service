# XSender - Validar y Consultar

> Consulta el estado de comprobantes y valida datos con los servicios web de SUNAT.

---

## 📋 Descripción

XSender permite consultar y validar comprobantes electrónicos utilizando los servicios web de SUNAT:

- **BillConsultService**: Consulta estado y CDR de comprobantes
- **BillValidService**: Valida datos de comprobantes

---

## 🔍 Consulta Comprobante (BillConsultService)

### Consulta estado de comprobante

```java
BillConsultServiceDestination destination = BillConsultServiceDestination.builder()
    .url("https://e-factura.sunat.gob.pe/ol-it-wsconscpegem/billConsultService")
    .operation(BillConsultServiceDestination.Operation.GET_STATUS)
    .build();

CamelData camelData = CamelUtils.getBillConsultService(
    "20494918910",      // RUC
    "01",               // Código de tipo de comprobante
    "F001",             // Serie del comprobante
    102,                // Número del comprobante
    destination,
    credentials
);

service.sunat.gob.pe.billconsultservice.StatusResponse sunatResponse = camelContext.createProducerTemplate()
    .requestBodyAndHeaders(
        Constants.XSENDER_BILL_CONSULT_SERVICE_URI,
        camelData.getBody(),
        camelData.getHeaders(),
        service.sunat.gob.pe.billconsultservice.StatusResponse.class
    );
```

### Consulta CDR de comprobante

```java
BillConsultServiceDestination destination = BillConsultServiceDestination.builder()
    .url("https://e-factura.sunat.gob.pe/ol-it-wsconscpegem/billConsultService")
    .operation(BillConsultServiceDestination.Operation.GET_STATUS_CDR)
    .build();

CamelData camelData = CamelUtils.getBillConsultService(
    "20494918910",      // RUC
    "01",               // Código de tipo de comprobante
    "F001",             // Serie del comprobante
    102,                // Número del comprobante
    destination,
    credentials
);

service.sunat.gob.pe.billconsultservice.StatusResponse sunatResponse = camelContext.createProducerTemplate()
    .requestBodyAndHeaders(
        Constants.XSENDER_BILL_CONSULT_SERVICE_URI,
        camelData.getBody(),
        camelData.getHeaders(),
        service.sunat.gob.pe.billconsultservice.StatusResponse.class
    );
```

---

## ✅ Valida Comprobante (BillValidService)

### Valida datos de comprobante

```java
BillValidServiceDestination destination = BillValidServiceDestination.builder()
    .url("https://e-factura.sunat.gob.pe/ol-it-wsconscpegem/billConsultService")
    .build();

CamelData camelData = getBillValidService(
    "20494918910",      // RUC
    "01",               // Código de tipo de comprobante
    "F001",             // Serie de comprobante
    "102",              // Número de comprobante
    "06",               // Tipo de documento de receptor (cliente)
    "12345678",         // Número de documento del receptor (cliente)
    "01-12-2022",       // Fecha de emisión del comprobante
    120.5,              // Importe total del comprobante
    "",                 // Campo debe de ir siempre vacío
    destination,
    credentials
);

service.sunat.gob.pe.billvalidservice.StatusResponse sunatResponse = camelContext.createProducerTemplate()
    .requestBodyAndHeaders(
        Constants.XSENDER_BILL_VALID_SERVICE_URI,
        camelData.getBody(),
        camelData.getHeaders(),
        service.sunat.gob.pe.billvalidservice.StatusResponse.class
    );
```

### Valida XML

```java
String fileName = "12345678912-01-F001-1.xml";
byte[] fileContent = // Lea su archivo XML en bytes[];

BillValidServiceDestination destination = BillValidServiceDestination.builder()
    .url("https://e-factura.sunat.gob.pe/ol-it-wsconscpegem/billConsultService")
    .build();

CamelData camelData = getBillValidService(
        fileName,
        fileContent,
        destination,
        credentials
);

service.sunat.gob.pe.billvalidservice.StatusResponse sunatResponse = camelContext
    .createProducerTemplate()
    .requestBodyAndHeaders(
        Constants.XSENDER_BILL_VALID_SERVICE_URI,
        camelData.getBody(),
        camelData.getHeaders(),
        service.sunat.gob.pe.billvalidservice.StatusResponse.class
    );
```

---

## 📊 Servicios Web de SUNAT

| Servicio | URL | Operaciones |
|----------|-----|-------------|
| **BillConsultService** | `https://e-factura.sunat.gob.pe/ol-it-wsconscpegem/billConsultService` | `GET_STATUS`, `GET_STATUS_CDR` |
| **BillValidService** | `https://e-factura.sunat.gob.pe/ol-it-wsconscpegem/billConsultService` | Validación de datos y XML |

---

## 📝 Códigos de Tipo de Comprobante

| Código | Tipo de Comprobante |
|--------|---------------------|
| `01` | Factura |
| `03` | Boleta de Venta |
| `07` | Nota de Crédito |
| `08` | Nota de Débito |

---

## 🔗 Ver También

- [Instalación](instalation.md)
- [Enviar XML](enviar-xml.md)
