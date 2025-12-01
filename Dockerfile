# ---------------------------
# STAGE 1: Build Frontend
# ---------------------------
FROM node:18 AS frontend-build
WORKDIR /app

# Copiar dependencias
COPY frontend/package*.json ./frontend/
WORKDIR /app/frontend
RUN npm install

# Copiar resto del frontend
COPY frontend /app/frontend

# Generar build
RUN npm run build


# ---------------------------
# STAGE 2: Build Backend
# ---------------------------
FROM maven:3.9.5-eclipse-temurin-17 AS backend-build
WORKDIR /app

# Copiar pom.xml y src del backend
COPY pom.xml ./
COPY src ./src

# Copiar build del frontend al backend
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

# Build backend con Maven
RUN mvn clean package -DskipTests


# ---------------------------
# STAGE 3: Runtime
# ---------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiar JAR final
COPY --from=backend-build /app/target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx1024m"
ENV SERVER_PORT=8080

EXPOSE 8080

USER 1000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
