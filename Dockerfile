# Use Tomcat 10.1 with JDK 21 as the base image
# Use a valid Tomcat 10.1 image with JDK 21
FROM tomcat:10.1.20-jdk21-temurin


# Set environment variables for Java options
ENV JAVA_OPTS="\
  -Dserver.port=9181 \
  -Djavax.net.ssl.keyStore=/config/keystore.jks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -Dlogging.file.name=/logs/app.log"

# Remove default webapps from Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR file into Tomcat's webapps directory
COPY build/libs/ips-client-v1-0.1.war /usr/local/tomcat/webapps/xml_signer_v01.war

# Expose the application port
EXPOSE 9181

# Start Tomcat
CMD ["catalina.sh", "run"]
