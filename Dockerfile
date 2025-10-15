# Use Tomcat 10.1 with JDK 21 as the base image
FROM tomcat:10.1.48-jdk21-temurin
RUN mkdir -p /var/log && chmod -R 777 /var/log

# Remove default webapps from Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file into Tomcat
COPY build/libs/ips-client-v1-0.1.war /usr/local/tomcat/webapps/xml_signer_v01.war

# Expose the application port
EXPOSE 8080

# Start Tomcat with runtime POD_NAME and logging path
ENTRYPOINT ["sh", "-c", "catalina.sh run \
  -Dserver.port=8080 \
  -Djavax.net.ssl.keyStore=/config/keystore.jks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -DPOD_NAME=$POD_NAME \
  -Dlogging.file.name=/var/log/app_$POD_NAME.log"]
