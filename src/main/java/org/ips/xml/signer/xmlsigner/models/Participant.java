package org.ips.xml.signer.xmlsigner.models;

import lombok.Data;

@Data
public class Participant {

    private String bic;

    private String userName;

    private String password;

    private String grantType;

    private String jwt;

    private String tokenGenerationPath;
}
