package org.ips.xml.signer.xmlsigner.service.apiClient;

import org.ips.xml.signer.xmlsigner.models.ParticipantCredentialInfo;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Service
public class TokenGenerationClientService {

    Logger logger = LoggerFactory.getLogger(TokenGenerationClientService.class);
    private RestTemplate restTemplate;
    HttpHeaders headers;
    private URI AUTH_SERVER_TOKEN_URI;


    @Autowired
    public TokenGenerationClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void create(String tokenUrl) {
        AUTH_SERVER_TOKEN_URI = URI.create(tokenUrl);
        headers = new HttpHeaders();
    }

    @Async("taskExecutor")
    public CompletableFuture<TokenInfo> generateTokenAsync(ParticipantCredentialInfo credentialInfo) {
        return CompletableFuture.supplyAsync(() -> {
            HttpHeaders headers = new HttpHeaders(); // Local to thread
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("jwt-assertion", credentialInfo.getJwt());

            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("username", credentialInfo.getUserName());
            requestBody.add("password", credentialInfo.getPassword());
            requestBody.add("grant_type", credentialInfo.getGrantType());

            try {
                ResponseEntity<TokenInfo> response = restTemplate.postForEntity(
                        credentialInfo.getTokenGenerationPath(),
                        new HttpEntity<>(requestBody, headers),
                        TokenInfo.class
                );
                return response.getBody();
            } catch (Exception e) {
                logger.error("Token generation failed: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
