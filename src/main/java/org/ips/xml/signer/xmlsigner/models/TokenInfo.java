package org.ips.xml.signer.xmlsigner.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenInfo implements Serializable {

    @JsonProperty("access_token")
    private String access_token;

    @JsonProperty("expires_in")
    private Long expires_in;

    @JsonProperty("refresh_expires_in")
    private Long refresh_expires_in;

    @JsonProperty("refresh_token")
    private String refresh_token;

    @JsonProperty("token_type")
    private String token_type;

    @JsonProperty("not-before-policy")
    private String not_before_policy;

    @JsonProperty("session_state")
    private String session_state;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("createdTm")

    private String createdTime;


}
