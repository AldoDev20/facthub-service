# XSender - Enviar XML

> Este tutorial sirve para enviar comprobantes de pago electrónicos a SUNAT.

---

## 📋 Descripción

Este tutorial cubre el envío de los siguientes comprobantes de pago:

- ✅ **Factura y Boleta**
- ✅ **Notas de crédito y débito**
- ✅ **Bajas y resúmenes diarios**
- ✅ **Percepciones y retenciones**
- ✅ **Guías de remisión**

---

## ⚙️ Paso 1: Configura URLs y credenciales

### URLs de la empresa

```java
CompanyURLs companyURLs = CompanyURLs.builder()
    .invoice("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
    .perceptionRetention("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
    .despatch("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem")
    .build();
```

### Credenciales de la empresa

```java
CompanyCredentials credentials = CompanyCredentials.builder()
    .username("12345678959MODDATOS")
    .password("MODDATOS")
    .token("accessTokenParaGuiasDeRemision")
    .build();
```

> ℹ️ **Nota:** XSender soporta el envío de **guías de remisión**. El `token` descrito en la clase `CompanyCredentials` representa el **access token** para el envío de guías de remisión. Lee [Obtener access token](#obtener-access-token-para-el-envío-de-guias-de-remisión).

---

## 📂 Paso 2: Analiza el XML que deseas enviar

```java
Path miXML = Paths.get("/home/mi_archivo_xml"); // El XML puede ser "Path, InputStream, o bytes[]"
BillServiceFileAnalyzer fileAnalyzer = new BillServiceXMLFileAnalyzer(miXML, companyURLs);

// Archivo ZIP
ZipFile zipFile = fileAnalyzer.getZipFile();

// Configuración para enviar XML
BillServiceDestination fileDestination = fileAnalyzer.getSendFileDestination();

// Configuración para consultar ticket
BillServiceDestination ticketDestination = fileAnalyzer.getVerifyTicketDestination();
```

---

## 📤 Paso 3: Enviar XML

```java
CamelContext camelContext = StandaloneCamel.getInstance()
    .getMainCamel()
    .getCamelContext();

CamelData camelData = CamelUtils.getBillServiceCamelData(zipFile, fileDestination, credentials);

SunatResponse sendFileSunatResponse = camelContext.createProducerTemplate()
    .requestBodyAndHeaders(
        Constants.XSENDER_BILL_SERVICE_URI,
        camelData.getBody(),
        camelData.getHeaders(),
        SunatResponse.class
    );
```

---

## 🎫 Paso 4: Consultar Ticket

Si enviaste una **baja**, **resumen diario**, o **guía de remisión**, entonces puedes consultar el estado de tu ticket.

```java
String ticket = sendFileSunatResponse.getSunat().getTicket();
CamelData camelTicketData = CamelUtils.getBillServiceCamelData(ticket, ticketDestination, credentials);

SunatResponse verifyTicketSunatResponse = camelContext.createProducerTemplate()
    .requestBodyAndHeaders(
        Constants.XSENDER_BILL_SERVICE_URI,
        camelTicketData.getBody(),
        camelTicketData.getHeaders(),
        SunatResponse.class
    );
```

---

## 🔑 Obtener access token para el envío de guías de remisión

```java
// Access token creado anteriormente (NULL si es la primera vez que generas el token).
// Si el token previo expiró entonces se genera uno nuevo.
// Si el token no expiró se devuelve el token previo.
ResponseAccessTokenSuccessDto prevToken = null;

String clientId = "myClientId";

Map<String, Object> headers = Map.of(
    HttpConstants.HTTP_URI, "https://api-cpe.sunat.gob.pe",
    HttpConstants.HTTP_PATH, "/v1/clientessol/" + clientId + "/oauth2/token/"
);

Object body = List.of(prevToken, Map.of(
    "grant_type", "password",
    "scope", "https://api-cpe.sunat.gob.pe",
    "client_id", clientId,
    "client_secret", "mySecret",
    "username", "12345678959MODDATOS",
    "password", "MODDATOS"
));

ResponseAccessTokenSuccessDto newToken = camelContext.createProducerTemplate()
    .requestBodyAndHeaders(
        Constants.XSENDER_CREDENTIALS_API_URI, 
        body, 
        headers, 
        ResponseAccessTokenSuccessDto.class
    );
```

---

## 🌐 URLs de SUNAT

| Servicio | URL |
|----------|-----|
| **Factura/Boleta/NC/ND (Beta)** | `https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService` |
| **Percepción/Retención (Beta)** | `https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService` |
| **Guías de Remisión** | `https://api-cpe.sunat.gob.pe/v1/contribuyente/gem` |

---

## 🔗 Ver También

- [Instalación](instalation.md)
- [Validar y Consultar](validar-consultar.md)
