FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/investment-service-0.0.1-SNAPSHOT.jar investment-service.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "investment-service.jar"]
