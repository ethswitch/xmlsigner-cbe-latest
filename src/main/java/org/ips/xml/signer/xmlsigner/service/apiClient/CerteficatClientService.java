package org.ips.xml.signer.xmlsigner.service.apiClient;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.ips.xml.signer.xmlsigner.models.CerteficateInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.CompletableFuture;


@Service
@Setter
@Slf4j
public class CerteficatClientService {
    Logger logger = LoggerFactory.getLogger(CerteficatClientService.class);
    private RestTemplate restTemplate;
    HttpHeaders headers;





    @Autowired
    public CerteficatClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

    }

    public void create() {

        headers = new HttpHeaders();
    }


    @Async("taskExecutor")
    public CompletableFuture<CerteficateInformation> downloadCerteficateAsync(CerteficateInformation certeficateInformation) {
        return CompletableFuture.supplyAsync(() -> {
            HttpHeaders headers = new HttpHeaders(); // Local to thread
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("Authorization", "Bearer " + certeficateInformation.getValidToken());

            String url = certeficateInformation.getCerteficateDownloadUrl() +
                    "?cert_iss=" + certeficateInformation.getCertificateIssuer() +
                    "&cert_sn=" + certeficateInformation.getCertificateSerialNumber();

            try {
                ResponseEntity<CerteficateInformation> response = restTemplate.exchange(
                        url, HttpMethod.GET, new HttpEntity<>(headers), CerteficateInformation.class
                );
                return response.getBody();
            } catch (Exception e) {
                logger.error("Certificate download failed: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }



}
