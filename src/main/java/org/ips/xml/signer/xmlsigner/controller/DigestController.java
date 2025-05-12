package org.ips.xml.signer.xmlsigner.controller;

import lombok.extern.slf4j.Slf4j;
import org.ips.xml.signer.xmlsigner.service.digestService.DigestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@Slf4j
public class DigestController {

    private final DigestService digestService;
    private final Executor signerExecutor;

    @Autowired
    public DigestController(DigestService digestService, Executor signerExecutor) {
        this.digestService = digestService;
        this.signerExecutor = signerExecutor;
    }

    @PostMapping(
            value = "/digest",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE}
    )
    public CompletableFuture<ResponseEntity<String>> handleXmlRequest(@RequestBody String request) {
        if (!isValidXml(request)) {
            log.warn("Invalid XML input");
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid XML input")
            );
        }

        return digestService.signDocumentAsync(request)
                .thenApply(signedXml -> ResponseEntity.ok().body(signedXml))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error processing XML"))
                .orTimeout(5, TimeUnit.SECONDS);
    }

    private boolean isValidXml(String xml) {
        return xml != null && xml.trim().startsWith("<?xml");
    }
}
