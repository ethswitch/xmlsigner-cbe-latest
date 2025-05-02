# Use Eclipse Temurin JDK 21 as the base image
FROM eclipse-temurin:21-jdk

# Set environment variables
ENV JAVA_OPTS="-Dserver.port=9181 \
    -Djavax.net.ssl.keyStore=/config/keystore.jks \
    -Djavax.net.ssl.keyStorePassword=changeit \
    -Dlogging.file.name=/logs/app.log"

# Create necessary directories
RUN mkdir -p /config /logs

# Copy the WAR file into the image
COPY build/libs/ips-client-v1-0.1.war /app/ips-client-v1-0.1.war

# Set the working directory
WORKDIR /app

# Expose the application's port
EXPOSE 9181

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar ips-client-v1-0.1.war"]
