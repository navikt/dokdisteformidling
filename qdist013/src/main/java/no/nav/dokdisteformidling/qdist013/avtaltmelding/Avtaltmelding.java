package no.nav.dokdisteformidling.qdist013.avtaltmelding;

import no.nav.dokdisteformidling.exception.functional.IkkeSammenfallendeIderFunctionalException;

import java.util.Map;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class Avtaltmelding {
	private final String journalpostId;
	private final String melding;
	private final Map<String, String> filnavnRegistry;

	public Avtaltmelding(String journalpostId, String melding, Map<String, String> filnavnRegistry) {
		if (isBlank(journalpostId) || isBlank(melding) || filnavnRegistry == null || filnavnRegistry.isEmpty()) {
			throw new IllegalArgumentException("journalpostId, melding, filnavnRegistry kan ikke være null eller tom");
		}
		this.journalpostId = journalpostId;
		this.melding = melding;
		this.filnavnRegistry = filnavnRegistry;
	}

	public String lookupFilnavn(String dokumentInfoId) {
		if (filnavnRegistry.containsKey(dokumentInfoId)) {
			return filnavnRegistry.get(dokumentInfoId);
		} else {
			throw new IkkeSammenfallendeIderFunctionalException(format("Finner ikke filnavn for dokumentInfoId=%s. journalpostId=%s", dokumentInfoId, journalpostId));
		}
	}

	public String asXmlString() {
		return melding;
	}

	public byte[] asBytes() {
		return melding.getBytes(UTF_8);
	}
}
