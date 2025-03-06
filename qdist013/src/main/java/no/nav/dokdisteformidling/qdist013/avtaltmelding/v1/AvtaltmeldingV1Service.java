package no.nav.dokdisteformidling.qdist013.avtaltmelding.v1;

import jakarta.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;
import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivmelding.Journalpost;
import no.nav.dokdisteformidling.qdist013.avtaltmelding.Avtaltmelding;
import no.nav.dokdisteformidling.qdist013.avtaltmelding.AvtaltmeldingService;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Profile("avtaltmelding_v1")
@Slf4j
@Component
public class AvtaltmeldingV1Service implements AvtaltmeldingService {

	private static final Pattern PATTERN_DOKUMENTREFERANSE = Pattern.compile("^\\d+-(?<dokumentInfoId>\\d+)-[a-zA-Z ]+-\\w+$");
	private final AvtaltmeldingV1Mapper avtaltmeldingV1Mapper;
	private final AvtaltmeldingV1Marshaller avtaltmeldingV1Marshaller;

	public AvtaltmeldingV1Service(AvtaltmeldingV1Mapper avtaltmeldingV1Mapper,
								  AvtaltmeldingV1Marshaller avtaltmeldingV1Marshaller) {
		this.avtaltmeldingV1Mapper = avtaltmeldingV1Mapper;
		this.avtaltmeldingV1Marshaller = avtaltmeldingV1Marshaller;
		log.info("Bruker AvtaltmeldingV1 som forretningsmelding til Trygderetten");
	}

	@Override
	public Avtaltmelding map(JournalpostQdist013 journalpostQdist013, String bestillingsId) {
		final JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingV1Mapper.createArkivMelding(journalpostQdist013, bestillingsId);
		final String arkivmeldingXmlString = avtaltmeldingV1Marshaller.marshal(arkivmeldingJAXBElement);
		Map<String, String> filnavnRegistry = buildFilnavnRegistry(arkivmeldingJAXBElement.getValue());
		return new Avtaltmelding(journalpostQdist013.getJournalpostId(), arkivmeldingXmlString, filnavnRegistry);
	}

	private Map<String, String> buildFilnavnRegistry(Arkivmelding arkivmelding) {
		// Journalpost:Arkivmelding er 1:1
		final Journalpost journalpost = (Journalpost) arkivmelding.getMappe().getFirst().getRegistrering().getFirst();
		return journalpost.getDokumentbeskrivelse().stream()
				.flatMap(dokumentbeskrivelse -> dokumentbeskrivelse.getDokumentobjekt().stream())
				.map(Dokumentobjekt::getReferanseDokumentfil)
				.collect(Collectors.toMap(referanseDokumentfil -> {
					Matcher matcher = PATTERN_DOKUMENTREFERANSE.matcher(referanseDokumentfil);
					if (matcher.matches()) {
						return matcher.group("dokumentInfoId");
					} else {
						throw new IllegalArgumentException("Klarte ikke matche referanseDokumentfil=" + referanseDokumentfil);
					}
				}, value -> value));
	}
}
