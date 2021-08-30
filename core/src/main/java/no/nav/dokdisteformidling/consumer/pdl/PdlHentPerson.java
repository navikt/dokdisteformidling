package no.nav.dokdisteformidling.consumer.pdl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@Builder
public class PdlHentPerson {

	private PDLHentPerson data;
	private List<PdlError> errors;

	@Data
	public static class PDLHentPerson {
		private HentPerson hentPerson;
	}

	@Data
	public static class HentPerson {
		private List<PersonNavn> navn;
		private List<Folkeregisteridentifikator> folkeregisteridentifikator;
	}

	@Data
	public static class PersonNavn {
		private String fornavn;
		private String mellomnavn;
		private String etternavn;
		private String forkortetNavn;
	}

	@Data
	public static class Folkeregisteridentifikator {
		@ToString.Exclude
		private String identifikasjonsnummer;
		private String type;
		private String status;
	}

	@Data
	@JsonIgnoreProperties({"locations", "path"})
	public static class PdlError {
		private String message;
		private PdlErrorExtensionTo extensions;
	}

	@Data
	static class PdlErrorExtensionTo {
		private String code;
		private ErrorDetails details;
		private String classification;
	}

	@Data
	static class ErrorDetails {
		private String type;
		private String cause;
		private String policy;
	}
}
