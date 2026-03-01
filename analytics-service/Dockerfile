FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/analytics-service-0.0.1-SNAPSHOT.jar analytics-service.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "analytics-service.jar"]
