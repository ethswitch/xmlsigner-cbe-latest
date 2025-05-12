package org.ips.xml.signer.xmlsigner.service;

import org.ips.xml.signer.xmlsigner.models.CerteficateInformation;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;
import org.ips.xml.signer.xmlsigner.repository.CacheRepository;
import org.ips.xml.signer.xmlsigner.repository.CertificateCacheRepository;
import org.ips.xml.signer.xmlsigner.service.apiClient.CerteficatClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class CertificateManager {
    private static final Logger logger = LoggerFactory.getLogger(CertificateManager.class);

    @Autowired
    private final CacheRepository cacheRepository;
    private final CertificateCacheRepository certificateCacheRepository;
    @Value("${ets.ips.certificate.download.url}")
    private String certeficateDownloadUrl;
    private TokenGenerationManager tokenGenerationManager;
    private CerteficatClientService certeficatClientService;

    public CertificateManager(CacheRepository cacheRepository, CertificateCacheRepository certificateCacheRepository,
                              TokenGenerationManager tokenGenerationManager,
                              CerteficatClientService certeficatClientService) {
        this.cacheRepository = cacheRepository;
        this.certificateCacheRepository = certificateCacheRepository;
        this.tokenGenerationManager = tokenGenerationManager;
        this.certeficatClientService = certeficatClientService;
    }


    @CacheEvict(value = "certificates", allEntries = true)
    public void clearAllCache() {
        System.out.println("clearing all catche");
    }


    @Async("taskExecutor")
    public CompletableFuture<CerteficateInformation> getCertificateAsync(CerteficateInformation certeficateInformation) {
        return CompletableFuture.supplyAsync(() -> getFromCache(certeficateInformation.getCertificateSerialNumber()))
                .thenCompose(cachedCert -> {
                    if (cachedCert == null) {
                        logger.info("Certificate not in cache. Fetching from API...");
                        return tokenGenerationManager.getTokenAsync()
                                .thenCompose(tokenInfo -> {
                                    certeficateInformation.setValidToken(tokenInfo.getAccess_token());
                                    certeficateInformation.setCerteficateDownloadUrl(certeficateDownloadUrl);
                                    return certeficatClientService.downloadCerteficateAsync(certeficateInformation);
                                })
                                .thenApply(cert -> {
                                    if (cert != null) {
                                        try {
                                            X509Certificate x509Cert = convertBase64StringToCerteficate(cert.getCertificate());
                                            certeficateInformation.setCertificate(cert.getCertificate());
                                            certeficateInformation.setX509Certificate(x509Cert);
                                            cacheRepository.put(certeficateInformation.getCertificateSerialNumber(), x509Cert);
                                            logger.info("Certificate fetched and cached: {}", cert);
                                        } catch (CertificateException e) {
                                            throw new RuntimeException("Certificate conversion failed", e);
                                        }
                                    }
                                    return certeficateInformation;
                                });
                    } else {
                        logger.debug("Certificate found in cache");
                        certeficateInformation.setCertificate(cachedCert.getCertificate());
                        certeficateInformation.setX509Certificate(cachedCert.getX509Certificate());
                        return CompletableFuture.completedFuture(certeficateInformation);
                    }
                });
    }

    @Async("taskExecutor")
    public CompletableFuture<RSAPublicKey> getPublicKeyAsync(CerteficateInformation certeficateInformation) {
        return getCertificateAsync(certeficateInformation)
                .thenApply(cert -> {
                    X509Certificate x509Cert = cert.getX509Certificate();
                    return (RSAPublicKey) x509Cert.getPublicKey();
                })
                .exceptionally(ex -> {
                    logger.error("Failed to get public key", ex);
                    return null;
                });
    }

    @Async("taskExecutor")
    public CompletableFuture<RSAPublicKey> getPublicKeyForMessageOrginator(CerteficateInformation certeficateInformation) {
        return getCertificateAsync(certeficateInformation)
                .thenApply(cert -> {
                    X509Certificate x509Cert = cert.getX509Certificate();
                    return (RSAPublicKey) x509Cert.getPublicKey();
                })
                .exceptionally(ex -> {
                    logger.error("Failed to retrieve public key", ex);
                    throw new RuntimeException(ex);
                });
    }
    public CerteficateInformation getFromCache(String serialNumber) {
        Optional<X509Certificate> s = cacheRepository.get(serialNumber);
        CerteficateInformation certeficateInformation = null;
        if (s.isPresent()) {
            logger.debug("Found the key in cache {} ", s.get());
            certeficateInformation = new CerteficateInformation();
            certeficateInformation.setX509Certificate(s.get());

        }
        return certeficateInformation;
    }

    public X509Certificate convertBase64StringToCerteficate(String certificateString) throws CertificateException {

        X509Certificate certificate = null;
        CertificateFactory cf = null;
        try {
            if (certificateString != null && !certificateString.trim().isEmpty()) {
                certificateString = certificateString.replace("-----BEGIN CERTIFICATE-----", "")
                        .replace("-----END CERTIFICATE-----", ""); // NEED FOR PEM FORMAT CERT STRING
                byte[] certificateData = Base64.getDecoder().decode(certificateString);
                cf = CertificateFactory.getInstance("X509");
                certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));

            }
        } catch (CertificateException e) {
            throw new CertificateException(e);
        }
        return certificate;
    }



}
