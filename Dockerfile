# Use Maven as the build image
FROM maven:3.9.9 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Use OpenJDK as the runtime image
FROM openjdk:21
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Set environment variables using the modern format
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]