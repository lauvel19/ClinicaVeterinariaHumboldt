# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar archivos de configuración Maven
COPY pom.xml ./
COPY src ./src

# Compilar el proyecto (saltando tests para build más rápido)
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/veterinaria-backend-*.jar app.jar

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xms256m -Xmx1024m"
ENV SERVER_PORT=8080

# Exponer puerto
EXPOSE 8080

# Usuario no-root para seguridad
USER 1000

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
