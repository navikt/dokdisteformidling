package no.nav.dokdisteformidling.consumer.pdl;

import no.nav.dokdisteformidling.consumer.pdl.PdlHentPerson.Folkeregisteridentifikator;
import no.nav.dokdisteformidling.consumer.pdl.PdlHentPerson.HentPerson;
import no.nav.dokdisteformidling.exception.functional.DokdistIllegalArgumentException;

import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

public final class HentPersonResponseMapper {

	private HentPersonResponseMapper() {
		// ingen instansiering
	}

	public static HentPersonInfo map(PdlHentPerson response) {
		if (isNull(response) || isNull(response.getData()) || isNull(response.getData().getHentPerson())) {
			throw new DokdistIllegalArgumentException("HentPerson kan ikke være null");
		}

		HentPerson hentPerson = response.getData().getHentPerson();

		return HentPersonInfo.builder()
				.ident(hentPerson.getFolkeregisteridentifikator().stream()
						.filter(Objects::nonNull)
						.map(Folkeregisteridentifikator::getIdentifikasjonsnummer)
						.findFirst()
						.orElseThrow(() -> new DokdistIllegalArgumentException("Folkeregisteridentifikator ikke funnet")))
				.fulltnavn(getFulltnavn(hentPerson))
				.build();
	}

	private static String getFulltnavn(HentPerson hentPerson) {
		if (isNull(hentPerson.getNavn()) || hentPerson.getNavn().isEmpty()) {
			throw new DokdistIllegalArgumentException("Personnavn kan ikke være null");
		}
		return hentPerson.getNavn().stream()
				.filter(HentPersonResponseMapper::isBlankForogEtternavn)
				.map(personNavn -> nonNull(personNavn.getFornavn()) ? trim(personNavn.getFornavn() + " " +
						(isBlank(personNavn.getMellomnavn()) ? "" : personNavn.getMellomnavn() + " ") +
						personNavn.getEtternavn()) : null).filter(Objects::nonNull)
				.findFirst().orElseThrow(() -> new DokdistIllegalArgumentException("Fornavn eller etternavn kan ikke være null"));
	}

	private static boolean isBlankForogEtternavn(PdlHentPerson.PersonNavn personNavn) {
		return isNotBlank(personNavn.getFornavn()) && isNotBlank(personNavn.getEtternavn());
	}
}
