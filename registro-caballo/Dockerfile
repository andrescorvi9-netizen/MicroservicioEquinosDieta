# ---- Etapa 1: compilar el .jar con Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Descargar dependencias primero (se reutilizan si el pom no cambia)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# application.properties no se sube a GitHub: en el servidor se usa la plantilla,
# que toma todos los valores sensibles de variables de entorno (sin valores por defecto).
RUN cp -n src/main/resources/application.properties.example src/main/resources/application.properties \
 && mvn -B -q package -DskipTests

# ---- Etapa 2: imagen liviana solo con Java para ejecutar ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Ajusta Java a instancias pequenas (512 MB en Render)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k"

# Render define la variable PORT; application.properties ya la lee con ${PORT:...}
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
