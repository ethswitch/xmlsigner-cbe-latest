package org.ips.xml.signer.xmlsigner.models;

import lombok.Data;

@Data
public class ServiceRequestHeader {
    private String access_token;
    private String certificateString;
}
