FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/money-manager-core-service-0.0.1-SNAPSHOT.jar money-manager-core-service-v1.0.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "money-manager-core-service-v1.0.jar"]