package no.nav.dokdisteformidling.qdist013.serviceregistry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InfoRecord {
	private String identifier;
	private String organizationName;

	@JsonCreator
	public InfoRecord(@JsonProperty("identifier") String identifier, @JsonProperty("organizationName") String organizationName) {
		this.identifier = identifier;
		this.organizationName = organizationName;
	}
}
