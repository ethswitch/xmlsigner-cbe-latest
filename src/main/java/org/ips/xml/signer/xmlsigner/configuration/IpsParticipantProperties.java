package org.ips.xml.signer.xmlsigner.configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.ips.xml.signer.xmlsigner.models.Participant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "ips")
public class IpsParticipantProperties {

    private List<Participant> participants = new ArrayList<>();

    public Optional<Participant> getByBic(String bic) {
        return participants.stream()
                .filter(p -> p.getBic().equalsIgnoreCase(bic))
                .findFirst();
    }

}
