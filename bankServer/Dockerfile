# Use an official Java image
FROM openjdk:19-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/*.jar app.jar

# Expose the port Spring Boot runs on (5001 as per your configuration)
EXPOSE 5001

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]