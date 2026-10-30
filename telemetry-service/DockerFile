FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY . .

RUN mvn -B -pl telemetry-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/telemetry-service/target/*-exec.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]