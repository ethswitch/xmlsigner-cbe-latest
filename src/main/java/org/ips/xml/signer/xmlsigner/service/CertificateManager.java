package org.ips.xml.signer.xmlsigner.service;

import org.ips.xml.signer.xmlsigner.models.CerteficateInformation;
import org.ips.xml.signer.xmlsigner.models.ServiceRequestHeader;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;
import org.ips.xml.signer.xmlsigner.repository.CacheRepository;
import org.ips.xml.signer.xmlsigner.repository.CertificateCacheRepository;
import org.ips.xml.signer.xmlsigner.service.apiClient.CerteficatClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Optional;

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


    public CerteficateInformation getCertificate(CerteficateInformation certeficateInformation,
                                                 ServiceRequestHeader serviceRequestHeader) throws CertificateException {

        CerteficateInformation cachedCeretficate = this.getFromCache(certeficateInformation.getCertificateSerialNumber());

        if (cachedCeretficate == null) {
            logger.info(" no cached certificate found and trying to download certificate");
            logger.info("calling the certeficate api");
            boolean isTokenMissing = serviceRequestHeader == null || !StringUtils.hasText(serviceRequestHeader.getAccess_token());
            TokenInfo tokenInfo = null;
            if (isTokenMissing) {
                logger.info(" token is not provided by service client and generating new one");
                tokenInfo = tokenGenerationManager.getToken();
            }

            certeficateInformation.setValidToken(tokenInfo.getAccess_token());
            certeficateInformation.setCerteficateDownloadUrl(this.certeficateDownloadUrl);
            CerteficateInformation cert = this.certeficatClientService.downloadCerteficate(certeficateInformation);
            if (cert != null) {
                certeficateInformation.setCertificate(cert.getCertificate());
                certeficateInformation.setX509Certificate(this.convertBase64StringToCerteficate(cert.getCertificate()));
                cacheRepository.put(certeficateInformation.getCertificateSerialNumber(), certeficateInformation.getX509Certificate());
            }
        } else {
            certeficateInformation.setCertificate(cachedCeretficate.getCertificate());
            certeficateInformation.setX509Certificate(cachedCeretficate.getX509Certificate());
        }
        return certeficateInformation;

    }

    public RSAPublicKey getPublicKeyForMessageOrginator(CerteficateInformation certificateInfo,
                                                         ServiceRequestHeader requestHeader) {
        try {
            CerteficateInformation resolvedCertInfo = resolveCertificate(certificateInfo, requestHeader);
            if (resolvedCertInfo == null || resolvedCertInfo.getX509Certificate() == null) {
                throw new RuntimeException("Certificate information is missing or invalid.");
            }

            PublicKey key = resolvedCertInfo.getX509Certificate().getPublicKey();
            if (!(key instanceof RSAPublicKey)) {
                throw new RuntimeException("Public key is not an RSA public key.");
            }

            return (RSAPublicKey) key;

        } catch (CertificateException e) {
            throw new RuntimeException("Failed to parse certificate", e);
        }
    }


    private CerteficateInformation resolveCertificate(CerteficateInformation defaultCertInfo,
                                                      ServiceRequestHeader requestHeader) throws CertificateException {
        if (requestHeader == null || !StringUtils.hasText(requestHeader.getCertificateString())) {
            return getCertificate(defaultCertInfo, requestHeader); // Fallback to system or DB lookup
        }

        X509Certificate cert = convertBase64StringToCerteficate(requestHeader.getCertificateString());
        CerteficateInformation providedCertInfo = new CerteficateInformation();
        providedCertInfo.setX509Certificate(cert);
        return providedCertInfo;
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
