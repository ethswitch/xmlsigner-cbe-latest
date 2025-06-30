package org.ips.xml.signer.xmlsigner.controller;


import org.ips.xml.signer.xmlsigner.models.TokenResponse;
import org.ips.xml.signer.xmlsigner.service.TokenGenerationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    @Autowired
    private TokenGenerationManager tokenService;

    @PostMapping("/generate")
    public ResponseEntity<TokenResponse> generateToken(@RequestParam String clientId) {
        try {
            return ResponseEntity.ok(tokenService.getToken(clientId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
