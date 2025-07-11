FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY src ./src

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Construir usando Maven wrapper
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR con el nombre correcto
COPY --from=build /app/target/application-0.0.1-SNAPSHOT.jar application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]
