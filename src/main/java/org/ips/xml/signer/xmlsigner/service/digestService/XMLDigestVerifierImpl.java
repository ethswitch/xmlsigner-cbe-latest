package org.ips.xml.signer.xmlsigner.service.digestService;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.ips.xml.signer.xmlsigner.models.CerteficateInformation;
import org.ips.xml.signer.xmlsigner.service.CertificateManager;
import org.ips.xml.signer.xmlsigner.utils.XMLFileUtility;
import org.ips.xml.signer.xmlsigner.utils.XmlSignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.util.concurrent.CompletableFuture;

@Setter
@Slf4j
@Service
@NoArgsConstructor
public class XMLDigestVerifierImpl implements XMLDigestVerifier {

    XMLFileUtility xmlFileUtility;

    CertificateManager certificateManager;

    private XmlSignUtil signUtil;

    public CompletableFuture<String> verifyAsync(String signedXml) {
        return CompletableFuture.supplyAsync(() -> {
            // Step 1: Parse XML into a Document
            try {
                return xmlFileUtility.createDocumentFromString(signedXml);
            } catch (Exception e) {
                log.error("Failed to parse XML document", e);
                throw new RuntimeException(e); // Propagate as unchecked exception
            }
        }).thenCompose(document -> {
            // Step 2: Extract certificate info from the Document
            CerteficateInformation certInfo = xmlFileUtility.parseCerteficateFromDocument(document);
            // Step 3: Fetch public key asynchronously
            return certificateManager.getPublicKeyAsync(certInfo)
                    .thenApply(publicKey -> {
                        // Step 4: Verify using the Document and PublicKey
                        try {
                            boolean isValid = signUtil.verify(document, publicKey);
                            return String.valueOf(isValid);
                        } catch (XMLSecurityException e) {
                            log.error("XML security error during verification", e);
                            return "false";
                        } catch (Exception e) {
                            log.error("Unexpected error during verification", e);
                            return "false";
                        }
                    });
        }).exceptionally(ex -> {
            log.error("Verification failed in async chain", ex);
            return "false";
        });
    }
    @Autowired
    public XMLDigestVerifierImpl(XMLFileUtility xmlFileUtility, CertificateManager certificateManager, XmlSignUtil signUtil) {
        this.xmlFileUtility = xmlFileUtility;
        this.certificateManager = certificateManager;
        this.signUtil = signUtil;
    }


    @Override
    public String verify(String signedXml) {
        Document document = xmlFileUtility.createDocumentFromString(signedXml);
        CerteficateInformation certInfo = xmlFileUtility.parseCerteficateFromDocument(document);

        // Use async method
        return certificateManager.getPublicKeyForMessageOrginator(certInfo)
                .thenApply(publicKey -> {
                    try {
                        return signUtil.verify(document, publicKey);
                    } catch (XMLSecurityException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenApply(String::valueOf)
                .exceptionally(ex -> "false")
                .join(); // Blocking for illustration; avoid in async flows
    }

    @Override
    public void clearCache() {
        certificateManager.clearAllCache();
    }
}
