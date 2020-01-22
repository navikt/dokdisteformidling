package no.nav.dokdisteformidling.qdist013.serviceregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentifierResource {
    private InfoRecord infoRecord;
    private List<ServiceRecord> serviceRecords;
}
