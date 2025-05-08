package org.ips.xml.signer.xmlsigner.service;

import javax.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PodNameMDCInitializer {

    @Value("${POD_NAME:unknown}")
    private String podName;

    @PostConstruct
    public void init() {
        MDC.put("POD_NAME", podName);
    }
}
