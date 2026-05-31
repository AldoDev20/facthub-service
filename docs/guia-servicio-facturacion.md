# Guía de Construcción: Microservicio de Facturación Electrónica

Esta guía detalla paso a paso cómo recrear desde cero el servicio de facturación construido con Spring Boot, XBuilder, XSender y Searchpe. Este documento te servirá como "plano arquitectónico" para replicarlo en tu proyecto final.

---

## 1. Dependencias Base (pom.xml)

El corazón del proyecto requiere importar las librerías oficiales del ecosistema `project-openubl` para la generación y envío de los comprobantes.

Abre tu archivo `pom.xml` y asegúrate de tener las siguientes dependencias:

```xml
<dependencies>
    <!-- Web (Para crear el servidor REST) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- Lombok (Para simplificar Getters/Setters) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- XBuilder: Generación de XML UBL -->
    <dependency>
        <groupId>io.github.project-openubl</groupId>
        <artifactId>xbuilder</artifactId>
        <version>5.0.2</version>
    </dependency>
    
    <!-- XSender: Envío de XML a SUNAT mediante Apache Camel -->
    <dependency>
        <groupId>io.github.project-openubl</groupId>
        <artifactId>spring-boot-xsender</artifactId>
        <version>5.0.2</version>
    </dependency>
</dependencies>
```

> **Configuración en Spring Boot:**
> En tu clase principal de Spring Boot (`Application.java`), debes decirle al framework que busque e inicialice los componentes internos de XSender (como Apache Camel). Se logra añadiendo el `@ComponentScan`:

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.tuempresa.facturacion", "io.github.project.openubl.spring.xsender.runtime"})
public class FacturacionApplication {
    public static void main(String[] args) {
        SpringApplication.run(FacturacionApplication.class, args);
    }
}
```

---

## 2. Generación del Certificado de Firma (.pfx)

La SUNAT exige que todo XML esté firmado criptográficamente. Para pruebas locales, debes generar un certificado auto-firmado y colocarlo en tu carpeta `src/main/resources/`.

**Comando en PowerShell:**
```powershell
$cert = New-SelfSignedCertificate -Subject "CN=Mi Empresa, OU=IT, O=Mi Empresa, C=PE" -CertStoreLocation Cert:\CurrentUser\My -KeyExportPolicy Exportable -KeySpec Signature
$pwd = ConvertTo-SecureString -String "miclave" -Force -AsPlainText
Export-PfxCertificate -Cert $cert -FilePath src\main\resources\certificado-prueba.pfx -Password $pwd
```
*Este comando creará el archivo `certificado-prueba.pfx` con la contraseña `miclave`.*

---

## 3. Capa de Modelos (DTOs)

Crea los objetos que transitarán la información entre tu cliente y el servidor.

**`ContribuyenteDto.java`** (Para mapear la respuesta de Searchpe):
```java
@Data
public class ContribuyenteDto {
    private String ruc;
    private String nombre;
    private String estado;
    private String condicion;
}
```

**`ItemDto.java`** (Para representar cada producto de la factura):
```java
@Data
public class ItemDto {
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
}
```

**`FacturaRequestDto.java`** (El body de la petición HTTP):
```java
@Data
public class FacturaRequestDto {
    private String rucCliente;
    private List<ItemDto> items;
}
```

---

## 4. Capa de Integración: Cliente Searchpe

Este servicio se encarga de corroborar el RUC y extraer el nombre (Razón Social) de la API simulada.

**`SearchpeClient.java`**:
```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class SearchpeClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContribuyenteDto obtenerContribuyentePorRuc(String ruc) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://searchpe-atelier.onrender.com/api/contribuyentes/" + ruc))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), ContribuyenteDto.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // O lanzar excepción personalizada
    }
}
```

---

## 5. Capa de Negocio: Construcción y Firma del XML

Este es el cerebro de la facturación. Integra XBuilder y la firma del XML.

**`FacturacionService.java`**:
```java
@Service
public class FacturacionService {

    private final SearchpeClient searchpeClient;

    public FacturacionService(SearchpeClient searchpeClient) {
        this.searchpeClient = searchpeClient;
    }

    public String generarYFirmarFacturaXml(FacturaRequestDto request) throws Exception {
        // 1. Obtener Razón Social consultando a Searchpe
        ContribuyenteDto clienteData = searchpeClient.obtenerContribuyentePorRuc(request.getRucCliente());
        if (clienteData == null || clienteData.getNombre() == null) {
            throw new RuntimeException("RUC no válido o no encontrado: " + request.getRucCliente());
        }

        // 2. Construir cabecera usando XBuilder
        var invoiceBuilder = Invoice.builder()
                .serie("F001")
                .numero(2) 
                .proveedor(Proveedor.builder()
                        .ruc("20123456789") // RUC Emisor
                        .razonSocial("MI EMPRESA S.A.C.")
                        .build()
                )
                .cliente(Cliente.builder()
                        .nombre(clienteData.getNombre())
                        .numeroDocumentoIdentidad(clienteData.getRuc())
                        .tipoDocumentoIdentidad(Catalog6.RUC.toString())
                        .build()
                );

        // 3. Agregar los productos de la petición de forma dinámica
        if (request.getItems() != null) {
            for (ItemDto item : request.getItems()) {
                invoiceBuilder.detalle(DocumentoVentaDetalle.builder()
                        .descripcion(item.getDescripcion())
                        .cantidad(item.getCantidad())
                        .precio(item.getPrecioUnitario())
                        .unidadMedida("ZZ") // "ZZ" es el estándar para Servicios
                        .build()
                );
            }
        }

        Invoice input = invoiceBuilder.build();

        // 4. Enriquecer (Cálculo automático de IGV, Total, etc)
        Defaults defaults = Defaults.builder().igvTasa(new BigDecimal("0.18")).build();
        ContentEnricher enricher = new ContentEnricher(defaults, LocalDate::now);
        enricher.enrich(input);

        // 5. Renderizar a XML Crudo
        String xmlCrudo = TemplateProducer.getInstance().getInvoice().data(input).render();

        // 6. Cargar certificado y FIRMAR el XML
        InputStream ksInputStream = getClass().getClassLoader().getResourceAsStream("certificado-prueba.pfx");
        CertificateDetails certificate = CertificateDetailsFactory.create(ksInputStream, "miclave");
        
        Document signedXML = XMLSigner.signXML(xmlCrudo, "MiEmpresa", certificate.getX509Certificate(), certificate.getPrivateKey());

        // 7. Convertir el XML Firmado a String
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(signedXML), new StreamResult(writer));

        return writer.toString();
    }
}
```

---

## 6. Capa de Envío a SUNAT (XSender)

Aquí empaquetamos el XML firmado en un ZIP y lo disparamos a SUNAT usando las integraciones de Camel.

**`SunatSenderService.java`**:
```java
import io.github.project.openubl.xsender.files.BillServiceXMLFileAnalyzer;
import io.github.project.openubl.xsender.sunat.BillServiceDestination;
import io.github.project.openubl.xsender.files.ZipFile;
import io.github.project.openubl.xsender.models.SunatResponse;

@Service
public class SunatSenderService {

    private final CamelContext camelContext;

    public SunatSenderService(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public SunatResponse enviarFactura(String xmlFirmado) throws Exception {
        // URLs del Entorno Beta SUNAT
        CompanyURLs companyURLs = CompanyURLs.builder()
                .invoice("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
                .build();

        // Credenciales oficiales de prueba SUNAT
        CompanyCredentials credentials = CompanyCredentials.builder()
                .username("12345678959MODDATOS")
                .password("MODDATOS")
                .build();

        // Crear XML temporal para que la librería lo lea
        Path tempXml = Files.createTempFile("factura-", ".xml");
        Files.writeString(tempXml, xmlFirmado);

        // Analizar y generar ZIP
        BillServiceFileAnalyzer fileAnalyzer = new BillServiceXMLFileAnalyzer(tempXml, companyURLs);
        ZipFile zipFile = fileAnalyzer.getZipFile();
        BillServiceDestination fileDestination = fileAnalyzer.getSendFileDestination();

        CamelData camelData = CamelUtils.getBillServiceCamelData(zipFile, fileDestination, credentials);

        // Envío SOAP a SUNAT
        SunatResponse response = camelContext.createProducerTemplate()
                .requestBodyAndHeaders(
                        Constants.XSENDER_BILL_SERVICE_URI,
                        camelData.getBody(),
                        camelData.getHeaders(),
                        SunatResponse.class
                );

        Files.deleteIfExists(tempXml);
        return response;
    }
}
```

---

## 7. Capa de Interfaz REST (El Controlador)

Finalmente, abrimos la puerta para que tu Backend Principal pueda consumir el microservicio mediante HTTP POST.

**`FacturacionController.java`**:
```java
@RestController
@RequestMapping("/api/factura")
public class FacturacionController {

    private final FacturacionService facturacionService;
    private final SunatSenderService sunatSenderService;

    public FacturacionController(FacturacionService facturacionService, SunatSenderService sunatSenderService) {
        this.facturacionService = facturacionService;
        this.sunatSenderService = sunatSenderService;
    }

    @PostMapping(value = "/emitir")
    public ResponseEntity<Map<String, Object>> emitirFactura(@RequestBody FacturaRequestDto request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Validar, Construir y Firmar XML
            String xmlFirmado = facturacionService.generarYFirmarFacturaXml(request);
            response.put("xmlFirmado", xmlFirmado);

            // 2. Transmitir a SUNAT
            SunatResponse sunatResponse = sunatSenderService.enviarFactura(xmlFirmado);
            
            // 3. Estructurar respuesta amigable
            Map<String, Object> sunatInfo = new HashMap<>();
            sunatInfo.put("status", sunatResponse.getStatus() != null ? sunatResponse.getStatus().name() : "UNKNOWN");
            if (sunatResponse.getSunat() != null) {
                sunatInfo.put("ticket", sunatResponse.getSunat().getTicket());
                sunatInfo.put("hasCdr", sunatResponse.getSunat().getCdr() != null);
            }
            response.put("sunatResponse", sunatInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
```

---

## 8. Testing Arquitectónico (ArchUnit)

Para asegurar que la arquitectura de microservicios y la separación de capas se mantenga limpia a medida que el proyecto crece, implementaremos **ArchUnit**.

**Añade la dependencia a tu `pom.xml`:**
```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```

**Ejemplo de prueba arquitectónica (`ArquitecturaTest.java`):**
Crea esta clase en la carpeta `src/test/java/com/tuempresa/facturacion/` para validar automáticamente las reglas de tu arquitectura:

```java
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.tuempresa.facturacion")
public class ArquitecturaTest {

    @ArchTest
    static final ArchRule servicios_no_deben_depender_de_controladores =
        noClasses().that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule controladores_deben_terminar_en_Controller =
        classes().that().resideInAPackage("..controller..")
            .should().haveSimpleNameEndingWith("Controller");
            
    @ArchTest
    static final ArchRule dtos_solo_deben_contener_data =
        classes().that().resideInAPackage("..dto..")
            .should().haveSimpleNameEndingWith("Dto");
}
```
Con ArchUnit proteges el código para que ningún desarrollador inyecte un controlador dentro de un servicio por error, garantizando calidad desde el inicio.

---

## 9. Arquitectura Definitiva: Microservicio + DDD

Al crear tu proyecto real, debes aplicar **Microservicios y DDD en conjunto**, ya que son conceptos complementarios que actúan en niveles distintos:

1. **Microservicios (Arquitectura Macro):** Tu sistema global separará responsabilidades. Este proyecto nacerá como un servicio independiente dedicado *exclusivamente* a la facturación, evitando que la complejidad (o lentitud) de SUNAT afecte a tu backend principal de ventas.
2. **Domain-Driven Design (Arquitectura Micro):** Dentro de este microservicio, organizaremos las carpetas bajo los principios de DDD. Esto separará tu lógica de negocio de los detalles técnicos (como las llamadas a SUNAT o Spring Boot).

### Bounded Contexts (Contextos Delimitados)

Dado el propósito de este microservicio, dividiremos el proyecto en **3 Bounded Contexts principales** más un núcleo compartido (`shared`). Cada contexto encapsulará su propia lógica (Dominio, Aplicación e Infraestructura):

1. **`billing` (Facturación):** *Core Domain*. Su única preocupación es aplicar las reglas de negocio para "armar" un comprobante válido. Se encarga de la validación de montos, impuestos, correlativos (`InvoiceSequence`) y generar la estructura UBL usando XBuilder.
2. **`transmission` (Transmisión / SUNAT):** *Core/Supporting Domain*. Su responsabilidad empieza donde termina `billing`. A este contexto no le importan los impuestos; su único trabajo es firmar el XML, enviarlo vía SOAP a SUNAT (XSender/Camel), parsear la respuesta (CDR/Ticket) y manejar colas de reintentos.
3. **`directory` (Directorio / Cliente):** *Supporting Domain*. Su trabajo es nutrir de información verídica al resto consultando APIs externas (Searchpe) para validar RUCs y obtener razones sociales.
4. **`shared` (Shared Kernel):** Para utilidades transversales, excepciones base y configuraciones genéricas de Spring Boot.

**Estructura de paquetes detallada (Mapeo de Clases):**

Para que sepas exactamente dónde mover el código que hemos construido en la prueba, aquí tienes el árbol expandido:

```text
com.tuempresa.facturacion
├── billing/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Invoice.java              # Entidad de dominio
│   │   │   ├── InvoiceItem.java          # Entidad de dominio
│   │   │   └── InvoiceSequence.java      # Para controlar los correlativos
│   │   ├── repository/
│   │   │   └── InvoiceRepository.java    # Interfaz
│   │   └── exception/
│   │       └── InvalidInvoiceException.java
│   ├── application/
│   │   ├── usecase/
│   │   │   └── GenerateInvoiceUseCase.java # Lógica central
│   │   └── dto/
│   │       ├── FacturaRequestDto.java
│   │       └── ItemDto.java
│   └── infrastructure/
│       ├── persistence/
│       │   └── JpaInvoiceRepository.java # Implementación Spring Data
│       └── xbuilder/
│           └── XBuilderFacturacionService.java # El FacturacionService que hicimos
│
├── transmission/
│   ├── domain/
│   │   ├── model/
│   │   │   └── SunatTicket.java
│   │   └── repository/
│   │       └── TransmissionLogRepository.java
│   ├── application/
│   │   └── usecase/
│   │       └── SendInvoiceToSunatUseCase.java
│   └── infrastructure/
│       ├── sunat/
│       │   └── XSenderSunatService.java  # El SunatSenderService que hicimos
│       └── config/
│           └── CamelConfig.java          # Configuración si Apache Camel la requiere
│
├── directory/
│   ├── domain/
│   │   └── model/
│   │       └── Taxpayer.java             # Entidad
│   ├── application/
│   │   └── usecase/
│   │       └── GetTaxpayerInfoUseCase.java
│   └── infrastructure/
│       └── searchpe/
│           ├── SearchpeClient.java       # El cliente HTTP que hicimos
│           └── dto/
│               └── ContribuyenteDto.java # DTO específico de la API externa
│
├── shared/
│   └── exception/
│       └── GlobalExceptionHandler.java   # Manejo de @ControllerAdvice
│
└── presentation/
    └── controller/
        └── FacturacionController.java    # El controlador REST final
```

**Beneficios de esta estructura orientada a Bounded Contexts:**
1. **Alta Cohesión:** Todo lo relacionado a comunicarse con SUNAT vive exclusivamente en `transmission`. Si SUNAT cambia de SOAP a REST, el contexto `billing` no se entera ni se modifica.
2. **Independencia del framework:** Tu lógica de negocio pura (`domain` y `application`) no dependerá de las librerías de `project-openubl`.
3. **Sinergia con ArchUnit:** Puedes crear reglas para asegurar que el contexto `billing` jamás importe directamente clases del contexto `transmission`.

---

## 10. Persistencia de Datos (PostgreSQL)

Para que el microservicio esté listo para producción, necesita su propia base de datos. Esta base de datos cumplirá tres roles fundamentales:
- **Gestión de Correlativos:** Saber qué número de factura toca emitir (F001-1, F001-2, etc.).
- **Almacenamiento Histórico:** Guardar un registro de los XML enviados y los archivos CDR devueltos por SUNAT (o sus URLs en la nube).
- **Cola de Reintentos:** Si SUNAT se cae, guardar la factura en estado `PENDING` para intentar enviarla automáticamente más tarde.

**Añade las dependencias a tu `pom.xml`:**
```xml
<!-- Spring Data JPA para manejo de base de datos -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Driver de PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Configuración de Credenciales (`src/main/resources/application.properties`):**
En tu nuevo proyecto, deberás añadir estas propiedades para conectar Spring Boot con tu base de datos PostgreSQL:

```properties
# ===============================
# CONFIGURACIÓN DE BASE DE DATOS (POSTGRESQL - AIVEN CLOUD)
# ===============================
spring.datasource.url=jdbc:postgresql://facthub-facthub.d.aivencloud.com:13247/defaultdb?sslmode=require
spring.datasource.username=${DB_USERNAME:avnadmin}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate ddl-auto (update para desarrollo, validate para producción)
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Esquema DDL (Ya creado en la Base de Datos)

**Nota Importante:** La base de datos configurada en Aiven **ya cuenta con** las siguientes tablas creadas y listas para usar. (Por eso hemos configurado `ddl-auto=validate` arriba, para que Spring solo valide que las entidades coincidan con las tablas sin intentar re-crearlas).

Este es el DDL con el que ya cuenta tu base de datos actualmente:

```sql
-- 1. Table to control invoice numbering sequence (e.g. F001 -> current number: 150)
CREATE TABLE invoice_sequence (
    series VARCHAR(4) PRIMARY KEY,
    last_number INTEGER NOT NULL DEFAULT 0
);

-- Insert F001 series starting at 0
INSERT INTO invoice_sequence (series, last_number) VALUES ('F001', 0);

-- 2. Main table to register the invoice and its status with SUNAT
CREATE TABLE invoice (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_type VARCHAR(2) NOT NULL,       -- '01' Invoice, '03' Receipt
    series VARCHAR(4) NOT NULL,              -- 'F001'
    number INTEGER NOT NULL,                 -- 151
    customer_ruc VARCHAR(11) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    issue_date TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Crucial fields for SUNAT lifecycle
    sunat_status VARCHAR(20) NOT NULL,       -- PENDING, ACCEPTED, REJECTED, EXCEPTION
    sunat_ticket VARCHAR(100),               -- Ticket returned by SUNAT (if applicable)
    
    -- Physical location of files in server or S3 bucket
    xml_file_path VARCHAR(500),
    cdr_file_path VARCHAR(500),
    
    -- Ensure no duplicate invoices are issued
    UNIQUE(series, number, document_type)
);
```

**¿Cómo interactúan y funcionan estas tablas en la vida real?**

### 1. La tabla `invoice_sequence` (Controlador de Números)
La SUNAT exige que tus facturas tengan una numeración estrictamente secuencial y sin saltos. 
Cuando llega la orden de emitir una nueva factura de la serie `F001`:
1. Tu código consulta: *"¿En qué número se quedó la serie F001?"*. La tabla responde: `last_number = 150`.
2. Tu código suma 1 y actualiza la tabla a `151`.
3. Garantizas así que el XML se llamará **F001-151**, evitando que dos ventas simultáneas choquen o dupliquen números.

### 2. La tabla `invoice` (El Registro Maestro y Máquina de Estados)
Es la "caja fuerte" donde guardas el resumen de la factura y su estado frente a SUNAT:
*   **Identificador Único (`series`, `number`, `document_type`):** La regla `UNIQUE` prohíbe guardar dos veces la misma factura por accidente.
*   **Datos de Negocio (`customer_ruc`, `total_amount`, etc.):** Resumen básico para que tu backend principal pueda consultar la factura sin tener que leer el XML físico.
*   **Máquina de Estados (`sunat_status`):** Es el corazón de la tolerancia a fallos.
    *   Apenas generas el XML, lo guardas con estado **`PENDING`**.
    *   Si el envío por SOAP tiene éxito, actualizas a **`ACCEPTED`** y guardas la ruta del CDR.
    *   Si se cae el internet o SUNAT está en mantenimiento, la factura se queda en **`PENDING`**. Un *Cron Job* automático puede buscar en esta tabla cada 10 minutos y reintentar el envío de los pendientes de forma silenciosa.
*   **Archivos Físicos (`xml_file_path`, `cdr_file_path`):** Guardan la ruta en tu servidor o nube (ej. S3) para futuras auditorías o descargas.

---

## 11. Flujo de Trabajo con Git y Conventional Commits

Para mantener el orden en el ciclo de vida del desarrollo de este microservicio, se establece una estrategia de ramificación basada en que el trabajo diario parte de la rama `develop`, combinada con la convención de **Conventional Commits** para tener un historial claro y automatizable.

### Estrategia de Ramas (Branching Strategy)

Tu rama de integración principal siempre será **`develop`**. Todas las nuevas características y correcciones nacerán de aquí:

*   **`main`** o **`master`**: Refleja el código que está actualmente desplegado en **Producción**. Nunca se commitea directamente aquí. Solo recibe *Merges* o *Pull Requests* desde `release` o `hotfix`.
*   **`develop`**: Rama principal de desarrollo e integración. Aquí conviven las últimas características que se subirán en la próxima versión.
*   **`feature/*`**: Ramas efímeras para desarrollar nuevas funcionalidades.
    *   **Parten de:** `develop`
    *   **Hacen merge a:** `develop`
    *   *Ejemplo:* `feature/add-postgresql-persistence`, `feature/implement-xml-signer`
*   **`bugfix/*`**: Para solucionar errores no críticos descubiertos en el entorno de pruebas/desarrollo.
    *   **Parten de:** `develop`
    *   **Hacen merge a:** `develop`
    *   *Ejemplo:* `bugfix/fix-igv-calculation`
*   **`release/*`**: Ramas de estabilización antes de pasar a producción.
    *   **Parten de:** `develop`
    *   **Hacen merge a:** `main` y `develop`
    *   *Ejemplo:* `release/v1.0.0`
*   **`hotfix/*`**: **(Excepción)** Ramas de emergencia para solucionar errores críticos en Producción.
    *   **Parten de:** `main`
    *   **Hacen merge a:** `main` y `develop`
    *   *Ejemplo:* `hotfix/sunat-certificate-expiration`

### Tus Primeras Ramas Exactas (Para empezar a programar)

Para que puedas empezar a construir este nuevo proyecto paso a paso, asumiendo que **ya tienes tu rama `develop` inicializada** con el commit `chore: initial commit with base structure`, aquí tienes **los nombres exactos de las ramas** que deberás ir creando (siempre partiendo de `develop`) y el orden lógico de integración:

1. `feature/setup-pom-dependencies`: Para añadir a tu estructura base las dependencias necesarias (XBuilder, XSender, PostgreSQL, ArchUnit, etc.).
2. `feature/setup-archunit`: Para crear las pruebas arquitectónicas antes de escribir lógica.
3. `feature/domain-entities`: Para crear las entidades de Dominio (`Invoice`, `InvoiceSequence`) y DTOs.
4. `feature/infra-postgresql-persistence`: Para configurar el `application.properties` y los repositorios de Spring Data JPA.
5. `feature/infra-searchpe-client`: Para implementar el cliente HTTP hacia la API de consulta de RUCs.
6. `feature/infra-sunat-xsender`: Para configurar Apache Camel, importar el `certificado-prueba.pfx` y preparar el empaquetado ZIP.
7. `feature/app-billing-usecase`: Para programar la lógica central con XBuilder que une todo.
8. `feature/presentation-rest-api`: Para crear el `FacturacionController` que expondrá el servicio a tu backend principal.

### Conventional Commits

Tus mensajes de commit deben seguir una estructura estandarizada para facilitar la lectura del historial y permitir la generación automática de *Changelogs*. El formato es:

`<tipo>(<alcance opcional>): <descripción breve>`

**Tipos permitidos:**
*   **`feat`**: Agrega una nueva característica al código (ej. un nuevo endpoint o tabla).
    *   *Ejemplo:* `feat(domain): add invoice and invoice_sequence entities`
*   **`fix`**: Soluciona un error o bug en el código.
    *   *Ejemplo:* `fix(sunat): correct beta endpoint URL in application properties`
*   **`chore`**: Tareas de mantenimiento rutinarias que no modifican el código fuente o las pruebas (ej. cambios en `pom.xml`, dependencias, o configuraciones del IDE).
    *   *Ejemplo:* `chore(deps): update xbuilder to version 5.0.2`
*   **`refactor`**: Cambios en el código que ni solucionan un error ni añaden una característica (ej. renombrar variables, extraer métodos).
    *   *Ejemplo:* `refactor(application): move dto classes to dedicated package`
*   **`docs`**: Cambios enfocados únicamente en la documentación (ej. actualizar README o esta misma guía).
    *   *Ejemplo:* `docs: add ddl script to documentation`
*   **`test`**: Agregar o modificar pruebas automatizadas (ArchUnit, JUnit).
    *   *Ejemplo:* `test(arch): add archunit rules for controllers and services`

Al aplicar esta convención y este flujo de ramas, tu repositorio estará listo para integrarse perfectamente con herramientas de CI/CD (como GitHub Actions o GitLab CI) de forma sumamente profesional.
