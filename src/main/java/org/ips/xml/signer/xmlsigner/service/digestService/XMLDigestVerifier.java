package org.ips.xml.signer.xmlsigner.service.digestService;

import java.util.concurrent.CompletableFuture;

public interface XMLDigestVerifier {
    public CompletableFuture<String> verifyAsync(String xmlRequest);

    public String verify(String signedXml);

    void clearCache();
}
