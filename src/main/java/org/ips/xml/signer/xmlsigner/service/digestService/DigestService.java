package org.ips.xml.signer.xmlsigner.service.digestService;

import java.util.concurrent.CompletableFuture;

public interface DigestService {

    String signDocument(String xmlString);
    public CompletableFuture<String> signDocumentAsync(String xmlString);

}
