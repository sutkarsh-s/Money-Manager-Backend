FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/email-service-0.0.1-SNAPSHOT.jar email-service.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "email-service.jar"]
