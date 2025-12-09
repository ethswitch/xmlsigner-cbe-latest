# Use Tomcat 10.1 with JDK 21 as the base image
FROM tomcat:10.1.20-jdk21-temurin

# Create log directory and set permissions
RUN mkdir -p /var/log && chmod -R 777 /var/log

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file and rename it to ROOT.war for root deployment
COPY build/libs/ips-client-v1-0.1.war /usr/local/tomcat/webapps/ROOT.war

# Change Tomcat HTTP port from 8080 to 9494
RUN sed -i 's/port="8080"/port="9494"/' /usr/local/tomcat/conf/server.xml

# Expose the new port
EXPOSE 9494

# Set environment variables (optional, but clean)
ENV CATALINA_OPTS="-Djavax.net.ssl.keyStore=/config/keystore.jks \
                   -Djavax.net.ssl.keyStorePassword=changeit \
                   -Dlogging.file.name=/var/log/app_${POD_NAME:-default}.log"

# Start Tomcat
ENTRYPOINT ["sh", "-c", "catalina.sh run $CATALINA_OPTS"]
