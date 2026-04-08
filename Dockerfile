# ============================================================
# STAGE 1: Build — compila el proyecto con Maven
# ============================================================
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiar archivos de configuración de Maven primero (mejor uso de caché)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x ./mvnw

# Descargar dependencias sin compilar (paso cacheado por separado)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar y empaquetar, saltando tests para acelerar el build
RUN ./mvnw package -DskipTests -B

# ============================================================
# STAGE 2: Runtime — imagen final ligera con solo el JAR
# ============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Crear usuario no-root para mayor seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Crear directorios necesarios con los permisos correctos
RUN mkdir -p /app/uploads /app/logs && \
    chown -R appuser:appgroup /app

# Copiar el JAR generado en el stage de build
COPY --from=build /app/target/*.jar app.jar

# Usar el usuario no privilegiado
USER appuser

# Exponer el puerto de la aplicación Spring Boot
EXPOSE 8080

# Punto de entrada: ejecutar el JAR con perfil de producción
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
