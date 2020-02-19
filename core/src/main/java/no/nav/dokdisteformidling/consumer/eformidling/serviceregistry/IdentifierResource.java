package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentifierResource {
    private InfoRecord infoRecord;
    private List<ServiceRecord> serviceRecords;

    Optional<ServiceRecord> findServiceRecord(final String process) {
        return serviceRecords.stream().filter(p -> process.equals(p.getProcess())).findAny();
    }

    static IdentifierResource empty() {
        final IdentifierResource identifierResource = new IdentifierResource();
        identifierResource.setInfoRecord(new InfoRecord(null, null));
        identifierResource.setServiceRecords(Collections.emptyList());
        return identifierResource;
    }
}
