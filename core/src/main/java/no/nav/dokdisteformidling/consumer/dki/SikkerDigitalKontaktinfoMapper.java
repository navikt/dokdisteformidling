package no.nav.dokdisteformidling.consumer.dki;

import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.DigitalPostkasse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.Epostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.Kontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.Mobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.SikkerDigitalKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.HentSikkerDigitalPostadresseResponse;

/**
 * @author Erik Bråten, Visma Consulting.
 */
public class SikkerDigitalKontaktinfoMapper {

	public HentSikkerDigitalPostadresseResponseTo map(HentSikkerDigitalPostadresseResponse response) {
		if (response == null) { return null; }
		SikkerDigitalKontaktinformasjon kontaktinfo = response.getSikkerDigitalKontaktinformasjon();
		return kontaktinfo == null ? null : HentSikkerDigitalPostadresseResponseTo.builder()
				.digitalKontaktinformasjon(mapKontaktinformasjon(kontaktinfo.getDigitalKontaktinformasjon()))
				.sikkerDigitalPostkasse(mapDigitalPostkasse(kontaktinfo.getSikkerDigitalPostkasse()))
				.sertifikat(kontaktinfo.getSertifikat())
				.build();
	}

	private HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon mapKontaktinformasjon(Kontaktinformasjon kontaktinformasjon) {
		return kontaktinformasjon == null ? null : HentSikkerDigitalPostadresseResponseTo.Kontaktinformasjon.builder()
				.personident(kontaktinformasjon.getPersonident())
				.reservasjon(kontaktinformasjon.getReservasjon())
				.epostadresse(mapEpostadresse(kontaktinformasjon.getEpostadresse()))
				.mobiltelefonnummer(mapMobiltelefonnummer(kontaktinformasjon.getMobiltelefonnummer()))
				.build();
	}

	private HentSikkerDigitalPostadresseResponseTo.Epostadresse mapEpostadresse(Epostadresse epostadresse) {
		return epostadresse == null ? null : HentSikkerDigitalPostadresseResponseTo.Epostadresse.builder()
				.value(epostadresse.getValue())
				.sistVerifisert(epostadresse.getSistVerifisert())
				.sistOppdatert(epostadresse.getSistOppdatert())
				.build();
	}

	private HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer mapMobiltelefonnummer(Mobiltelefonnummer mobiltelefonnummer) {
		return mobiltelefonnummer == null ? null : HentSikkerDigitalPostadresseResponseTo.Mobiltelefonnummer.builder()
				.value(mobiltelefonnummer.getValue())
				.sistVerifisert(mobiltelefonnummer.getSistVerifisert())
				.sistOppdatert(mobiltelefonnummer.getSistOppdatert())
				.build();
	}

	private HentSikkerDigitalPostadresseResponseTo.DigitalPostkasse mapDigitalPostkasse(DigitalPostkasse digitalPostkasse) {
		return digitalPostkasse == null ? null : HentSikkerDigitalPostadresseResponseTo.DigitalPostkasse.builder()
				.leverandoerAdresse(digitalPostkasse.getLeverandoerAdresse())
				.brukerAdresse(digitalPostkasse.getBrukerAdresse())
				.build();
	}
}
