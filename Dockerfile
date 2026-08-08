# Use an official lightweight JDK runtime base image
FROM eclipse-temurin:21-jre-jammy

# Set the internal working directory
WORKDIR /app

# Copy the built JAR file from your build directory into the container
COPY target/*.jar app.jar

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]