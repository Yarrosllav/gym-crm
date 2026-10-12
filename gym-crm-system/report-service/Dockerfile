FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY gym-crm-service/pom.xml gym-crm-service/pom.xml
COPY report-service/pom.xml report-service/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml

RUN mvn -B -pl report-service -am dependency:go-offline

COPY report-service/src report-service/src

RUN mvn -B -pl report-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build \
  /workspace/report-service/target/report-service-1.0-SNAPSHOT.jar \
  /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
