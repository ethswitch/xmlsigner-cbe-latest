package org.ips.xml.signer.xmlsigner.models;

public class TokenResponse {
    private String access_token;

    public TokenResponse(String token) {
        this.access_token = token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
