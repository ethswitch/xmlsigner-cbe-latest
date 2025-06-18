package org.ips.xml.signer.xmlsigner.service.digestService;

import org.ips.xml.signer.xmlsigner.models.ServiceRequestHeader;
import org.ips.xml.signer.xmlsigner.models.TokenInfo;

public interface XMLDigestVerifier {

    public String verify(String signedXml, ServiceRequestHeader serviceRequestHeader);

    void clearCache();
}
