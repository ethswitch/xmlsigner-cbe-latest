package org.ips.xml.signer.xmlsigner.configuration;


import ch.qos.logback.core.PropertyDefinerBase;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CurrentDateDefiner extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
