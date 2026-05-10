FROM maven:3.9-eclipse-temurin-21 AS build

ARG SERVICE_DIR
WORKDIR /workspace

COPY ${SERVICE_DIR}/pom.xml ./pom.xml
COPY ${SERVICE_DIR}/mvnw ./mvnw
COPY ${SERVICE_DIR}/.mvn ./.mvn
COPY ${SERVICE_DIR}/src ./src

RUN chmod +x ./mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
