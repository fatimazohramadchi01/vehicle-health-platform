FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY . .

RUN mvn -B -pl vehicle-analysis-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/vehicle-analysis-service/target/*-exec.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]