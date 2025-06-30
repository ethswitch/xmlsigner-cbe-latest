package org.ips.xml.signer.xmlsigner.service.token;

import org.ips.xml.signer.xmlsigner.models.TokenInfo;

public interface TokenCacheService {

    TokenInfo getToken(String clientId);


    void saveToken(String clientId, TokenInfo token);

    void removeToken(String clientId);

}