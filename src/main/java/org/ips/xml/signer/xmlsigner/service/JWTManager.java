package org.ips.xml.signer.xmlsigner.service;

import org.ips.xml.signer.xmlsigner.models.JWTInfo;
import org.ips.xml.signer.xmlsigner.utils.JwtSigningUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JWTManager {

    private JwtSigningUtils jwtSigningUtils;

    @Autowired
    public JWTManager(JwtSigningUtils jwtSigningUtils) {
        this.jwtSigningUtils = jwtSigningUtils;
    }

    public JWTInfo getJWT(String bankBic) {
        JWTInfo jwtInfo = new JWTInfo();
        try {
            jwtInfo.setParticipantBic(bankBic);
            jwtSigningUtils.generateJwt(jwtInfo,bankBic);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return jwtInfo;
    }
}
