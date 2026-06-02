# Etapa 1: Build
FROM eclipse-temurin:26-jdk-alpine AS builder

WORKDIR /app

# Copiamos primero el wrapper y el pom.xml para descargar dependencias
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Damos permisos de ejecución al wrapper
RUN chmod +x mvnw

# Descargamos dependencias (aprovechando la caché de Docker)
RUN ./mvnw dependency:go-offline -B

# Copiamos el código fuente
COPY src ./src

# Compilamos el proyecto omitiendo los tests (ya que no hay base de datos en esta fase)
RUN ./mvnw clean package -DskipTests

# Etapa 2: Run (Imagen ultra ligera solo para ejecución)
FROM eclipse-temurin:26-jre-alpine

WORKDIR /app

# Copiamos únicamente el .jar final de la etapa de construcción
COPY --from=builder /app/target/billing-service-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto
EXPOSE 8080

# Configuramos variables de entorno seguras recomendadas para Spring Boot en Docker
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Punto de entrada
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
