package org.ips.xml.signer.xmlsigner.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.ips.xml.signer.xmlsigner.models.ServiceRequestHeader;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;
import org.ips.xml.signer.xmlsigner.service.digestService.XMLDigestVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class MessageDigestVerificationController {

    private XMLDigestVerifier digestVerifier;

    @Autowired
    public MessageDigestVerificationController(XMLDigestVerifier digestVerifier) {
        this.digestVerifier = digestVerifier;
    }

    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_XML_VALUE)
    public String verifyXml(HttpServletRequest servletRequest, @RequestBody String request) {
        ServiceRequestHeader requestHeader = new ServiceRequestHeader();
        ;
        String accessToken = servletRequest.getHeader("access_token");
        String certificateString = servletRequest.getHeader("X-Certificate");
        if (StringUtils.hasText(accessToken)) {
            requestHeader.setAccess_token(accessToken);
        }
        if (StringUtils.hasText(certificateString)) {
            requestHeader.setCertificateString(certificateString);
        }
        String xmlResponse = digestVerifier.verify(request, requestHeader);
        xmlResponse = xmlResponse.replace("&#xD;", "");
        return xmlResponse;
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
