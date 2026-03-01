FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/planning-service-0.0.1-SNAPSHOT.jar planning-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "planning-service.jar"]
