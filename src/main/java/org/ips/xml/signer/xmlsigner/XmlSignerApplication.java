package org.ips.xml.signer.xmlsigner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@EnableScheduling
@EnableRetry
// REMOVE this line or modify it:
// @ComponentScan(basePackages = {"org.ips.xml.signer.xmlsigner"})
public class XmlSignerApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(XmlSignerApplication.class);
    }

    public static void main(String[] args) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.setProperty("CURRENT_DATE", currentDate);
        SpringApplication.run(XmlSignerApplication.class, args);
    }
}