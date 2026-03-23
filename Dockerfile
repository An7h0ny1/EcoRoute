# Etapa 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Copiar solo el pom.xml primero para aprovechar la caché de Docker de las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear un usuario no-root por seguridad (Práctica de Arquitectura Profesional)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Configuración de memoria optimizada para contenedores
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]