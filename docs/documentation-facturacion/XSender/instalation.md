# XSender - Instalación

> XSender puede ser usado y descargado desde el repositorio central de Maven.

---

## 📋 Descripción

XSender es una librería Java que permite enviar y consultar comprobantes electrónicos a SUNAT. Las versiones pueden ser consultadas en [Maven Central](https://mvnrepository.com/artifact/io.github.project-openubl/xsender).

---

## 📦 Instalación por Framework

### 🎯 Standalone

> Ideal para aplicaciones de escritorio o aplicaciones desplegadas en servidores como Tomcat.

**Maven - pom.xml:**

```xml
<dependency>
    <groupId>io.github.project-openubl</groupId>
    <artifactId>xsender</artifactId>
    <version>${xsender.version}</version>
</dependency>
```

**Inicializa CamelContext y utilízalo con XSender:**

```java
public class XSenderController {

    public String test() {
        CamelContext camelContext = StandaloneCamel.getInstance()
            .getMainCamel()
            .getCamelContext();

        SunatResponse sendFileSunatResponse = camelContext.createProducerTemplate()
                .requestBodyAndHeaders(
                        Constants.XSENDER_BILL_SERVICE_URI,
                        camelData.getBody(),
                        camelData.getHeaders(),
                        SunatResponse.class
                );
    }
}
```

---

### 🚀 Quarkus

> Para aplicaciones hechas en Quarkus.

**Maven - pom.xml:**

```xml
<dependency>
    <groupId>io.github.project-openubl</groupId>
    <artifactId>quarkus-xsender</artifactId>
    <version>${xsender.version}</version>
</dependency>
```

**Inyecta CamelContext y utilízalo con XSender:**

```java
@ApplicationScoped
public class XSenderController {

    @Inject
    private CamelContext camelContext;

    public String test() {
        SunatResponse sendFileSunatResponse = camelContext.createProducerTemplate()
                .requestBodyAndHeaders(
                        Constants.XSENDER_BILL_SERVICE_URI,
                        camelData.getBody(),
                        camelData.getHeaders(),
                        SunatResponse.class
                );
    }
}
```

---

### 🌱 Spring Boot

> Para aplicaciones hechas en Spring Boot.

**Maven - pom.xml:**

```xml
<dependency>
    <groupId>io.github.project-openubl</groupId>
    <artifactId>spring-boot-xsender</artifactId>
    <version>${xsender.version}</version>
</dependency>
```

**Configura la clase principal de Spring Boot:**

```java
@ComponentScan("io.github.project.openubl.spring.xsender.runtime")
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Inyecta CamelContext y utilízalo con XSender:**

```java
public class XSenderController {

    @Autowired
    private CamelContext camelContext;

    public String test() {
        SunatResponse sendFileSunatResponse = camelContext.createProducerTemplate()
                .requestBodyAndHeaders(
                        Constants.XSENDER_BILL_SERVICE_URI,
                        camelData.getBody(),
                        camelData.getHeaders(),
                        SunatResponse.class
                );
    }
}
```

---

### 🐘 Gradle

**build.gradle:**

```groovy
compile group: 'io.github.project-openubl', name: 'xsender', version: 'VERSION'
```

#### Configuración extra para Gradle

> ⚠️ **Importante:** XSender usa **Apache CXF** y Gradle no es capaz de descargar algunas dependencias, por lo tanto es necesario agregar dependencias manualmente.

Para mayor información lee: [Apache CXF missing dependencies when coming from Gradle](#)

```groovy
runtimeOnly("jakarta.xml.soap:jakarta.xml.soap-api:1.4.2")
runtimeOnly("jakarta.xml.ws:jakarta.xml.ws-api:2.3.3")
runtimeOnly("jakarta.annotation:jakarta.annotation-api:1.3.5")
```

---

## 📊 Resumen de Dependencias

| Framework | Artifact | Uso |
|-----------|----------|-----|
| **Standalone** | `xsender` | Aplicaciones de escritorio, Tomcat |
| **Quarkus** | `quarkus-xsender` | Aplicaciones Quarkus |
| **Spring Boot** | `spring-boot-xsender` | Aplicaciones Spring Boot |
| **Gradle** | `xsender` | Proyectos Gradle (requiere dependencias extra) |

---

## 🔗 Ver También

- [Enviar XML](enviar-xml.md)
- [Validar y Consultar](validar-consultar.md)
