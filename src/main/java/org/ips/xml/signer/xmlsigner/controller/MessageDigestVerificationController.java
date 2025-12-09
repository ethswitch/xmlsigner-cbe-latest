package org.ips.xml.signer.xmlsigner.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.ips.xml.signer.xmlsigner.models.ServiceRequestHeader;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;
import org.ips.xml.signer.xmlsigner.service.digestService.XMLDigestVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@Slf4j
public class MessageDigestVerificationController {

    private XMLDigestVerifier digestVerifier;

    @Autowired
    public MessageDigestVerificationController(XMLDigestVerifier digestVerifier) {
        this.digestVerifier = digestVerifier;
    }

    @PostMapping(
            value = "/verify",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> verifyXml(
            HttpServletRequest request,
            @RequestBody String xmlPayload
    ) {
        ServiceRequestHeader header = new ServiceRequestHeader();

        try {
            // Extract headers safely
            String accessToken = request.getHeader("access_token");
            String certificateString = request.getHeader("X-Certificate");
            String bankBic = request.getHeader("bank_bic");

            if (StringUtils.hasText(accessToken)) {
                header.setAccess_token(accessToken);
            }
            if (StringUtils.hasText(certificateString)) {
                header.setCertificateString(certificateString);
            }
            header.setBankBic(bankBic);

            // Call verification service
            String result = digestVerifier.verify(xmlPayload, header);


            // Expecting result "true" or "false"
            boolean isValidXml = Boolean.parseBoolean(result);

            if (!isValidXml) {
                // Invalid → return 400
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("false");
            }

            // Valid → return 200
            return ResponseEntity
                    .ok("true");

        } catch (Exception ex) {
            // Log error
            log.error("XML verification failed: {}", ex.getMessage(), ex);

            // Return safe 400 response
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("false");
        }
    }



    @PostMapping(value = "/evictCache")
    public String evictCach() {

        digestVerifier.clearCache();
        return "evictede succ";

    }

    // Example XML validation function
    private boolean isValidXml(String xml) {
        return xml != null && xml.trim().startsWith("<?xml");
    }
}
