package no.nav.dokdisteformidling.qdist013.serviceregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

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
}
