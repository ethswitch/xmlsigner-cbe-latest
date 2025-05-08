package org.ips.xml.signer.xmlsigner.service;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PodNameMDCInitializer {
    @Value("${POD_NAME:unknown}") // or ${HOSTNAME}
    private String podName;

    @PostConstruct
    public void init() {
        MDC.put("POD_NAME", podName); // Add to MDC
        log.info("Pod name initialized: {}", podName);
    }
}