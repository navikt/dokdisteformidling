package no.nav.dokdisteformidling.common;

import static java.lang.String.format;
import static org.jvnet.jaxb2_commons.lang.StringUtils.isEmpty;

import no.nav.dokdisteformidling.exception.functional.IntegrasjonspunktRequestFunctionalException;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting AS
 */
public final class IntegrasjonspunktAssertionUtils {

	private IntegrasjonspunktAssertionUtils() {
	}

	public static void assertObjectOnIntegrasjonspunktBodyNotNull(String field, Object objectValue) {
		if (objectValue == null) {
			throw new IntegrasjonspunktRequestFunctionalException(format("Feltet %s ikke være null i journalpost-respons fra SAF.", field));
		}
	}

	public static void assertFieldOnIntegrasjonspunktBodyNotNullOrEmpty(String field, String value) {
		if (isEmpty(value)) {
			throw new IntegrasjonspunktRequestFunctionalException(format("Feltet %s ikke være null eller tomt i journalpost-respons fra SAF.", field));
		}
	}
}
