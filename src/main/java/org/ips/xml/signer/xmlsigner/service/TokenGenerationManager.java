package org.ips.xml.signer.xmlsigner.service;

import lombok.extern.slf4j.Slf4j;
import org.ips.xml.signer.xmlsigner.models.JWTInfo;
import org.ips.xml.signer.xmlsigner.models.ParticipantCredentialInfo;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;
import org.ips.xml.signer.xmlsigner.service.apiClient.TokenGenerationClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class TokenGenerationManager {


    @Value("${ets.ips.token.url}")
    private String tokenUrl;
    @Value("${ets.ips.grantType}")
    private String grantType;
    @Value("${ets.ips.userName}")
    private String userName;
    @Value("${ets.ips.password}")
    private String password;

    TokenGenerationClientService service;
    JWTManager jwtManager;

    @Autowired
    public TokenGenerationManager(TokenGenerationClientService service, JWTManager jwtManager) {
        this.service = service;
        this.jwtManager = jwtManager;
    }


    @Async("taskExecutor")
    public CompletableFuture<TokenInfo> getTokenAsync() {
        // 1. Prepare credentials
        ParticipantCredentialInfo credentialInfo = new ParticipantCredentialInfo();
        JWTInfo jwtInfo = jwtManager.getJWT();
        if (jwtInfo == null) {
            return CompletableFuture.failedFuture(new RuntimeException("JWT not available"));
        }
        credentialInfo.setUserName(userName);
        credentialInfo.setPassword(password);
        credentialInfo.setGrantType(grantType);
        credentialInfo.setTokenGenerationPath(tokenUrl);
        credentialInfo.setJwt(jwtInfo.getJwt());

        // 2. Chain async token generation
        return service.generateTokenAsync(credentialInfo)
                .exceptionally(ex -> {
                    log.error("Token generation failed", ex);
                    throw new RuntimeException("Token generation failed", ex);
                });
    }
}
