# Use Java 17 to match your pom.xml (not 21)
FROM eclipse-temurin:21-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy the Spring Boot executable JAR with correct artifact name
COPY target/GarageApi-0.1.jar app.jar

# Expose the default port
EXPOSE 8020
# Set environment variable inside container
ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/auto_adda_garage?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true

# Run the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]