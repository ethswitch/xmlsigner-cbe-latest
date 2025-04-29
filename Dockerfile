# Use a Tomcat base image with JDK 21
FROM tomcat:9.0-jdk21

# Set environment variables
ENV JAVA_OPTS="-Dserver.port=9181 \
    -Djavax.net.ssl.keyStore=/config/keystore.jks \
    -Djavax.net.ssl.keyStorePassword=changeit \
    -Dlogging.file.name=/logs/app.log"

# Clean default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy your WAR file to Tomcat webapps
COPY build/libs/ips-client-v1-0.1.war /usr/local/tomcat/webapps/xml_signer_v01.war

# Expose the app's internal port
EXPOSE 9181

# Start Tomcat
CMD ["catalina.sh", "run"]

# Suggested build command with image tag:
# docker build -t your_dockerhub_username/xml-signer-app:0.1 .
