package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ServiceRecord {
	private String organisationNumber;
	private String pemCertificate;
	private String process;
	private List<String> documentTypes;
	private Service service;

	@JsonCreator
	public ServiceRecord(@JsonProperty("organisationNumber") String organisationNumber ,
                         @JsonProperty("pemCertificate") String pemCertificate,
                         @JsonProperty("process") String process,
                         @JsonProperty("documentTypes") List<String> documentTypes,
                         @JsonProperty("service") Service service) {
		this.organisationNumber = organisationNumber;
		this.pemCertificate = pemCertificate;
        this.process = process;
        this.documentTypes = documentTypes;
        this.service = service;
    }
}
