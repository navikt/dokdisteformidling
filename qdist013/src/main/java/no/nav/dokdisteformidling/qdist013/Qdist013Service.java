package no.nav.dokdisteformidling.qdist013;

import static no.nav.dokdisteformidling.common.FunctionalUtils.deserializeS3JsonPayloadToDokdistDokument;
import static no.nav.dokdisteformidling.common.FunctionalUtils.generateRandomUUID;
import static no.nav.dokdisteformidling.common.FunctionalUtils.validateThatForsendelseStatusIsKlarForDist;

import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.nav.dokdisteformidling.consumer.integrasjonspunkt.Integrasjonspunkt;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeMarshalleArkivmeldingTechnicalException;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.S3Storage;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class Qdist013Service {

	private final S3Storage s3Storage;
	private final AdministrerForsendelse administrerForsendelse;
	private final SafJournalpostQueryService<JournalpostQdist013> safJournalpostQueryService;
	private final JuridiskLogg juridiskLogg;
	private final Integrasjonspunkt integrasjonspunkt;
	private final LagreJuridiskLoggMapper lagreJuridiskLoggMapper;
	private final ArkivmeldingMapper arkivmeldingMapper;
	private final CreateMessageRequestMapper createMessageRequestMapper;

	public Qdist013Service(S3Storage s3Storage,
						   AdministrerForsendelse administrerForsendelse,
						   @Named("SafJournalpostQueryServiceQdist013") SafJournalpostQueryService<JournalpostQdist013> safJournalpostQueryService,
						   JuridiskLogg juridiskLogg,
						   Integrasjonspunkt integrasjonspunkt,
						   LagreJuridiskLoggMapper lagreJuridiskLoggMapper,
						   ArkivmeldingMapper arkivmeldingMapper,
						   CreateMessageRequestMapper createMessageRequestMapper) {
		this.s3Storage = s3Storage;
		this.administrerForsendelse = administrerForsendelse;
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.juridiskLogg = juridiskLogg;
		this.integrasjonspunkt = integrasjonspunkt;
		this.lagreJuridiskLoggMapper = lagreJuridiskLoggMapper;
		this.arkivmeldingMapper = arkivmeldingMapper;
		this.createMessageRequestMapper = createMessageRequestMapper;
	}

	@Handler
	public void processForsendelse(DistribuerForsendelseTilTrygderetten distribuerForsendelseTilTrygderetten) {
		final String conversationId = generateRandomUUID(); //TODO Add as camelProp and logg?
		final HentForsendelseResponseTo hentForsendelseResponseTo = administrerForsendelse.hentForsendelse(distribuerForsendelseTilTrygderetten
				.getForsendelseId());
		validateThatForsendelseStatusIsKlarForDist(hentForsendelseResponseTo.getForsendelseStatus());

		final List<DokdistDokument> dokdistDokumentList = getDocumentsFromS3(hentForsendelseResponseTo);

		final JournalpostQdist013 journalpostQdist013 = safJournalpostQueryService.hentJournalpost(hentForsendelseResponseTo.getArkivInformasjon()
				.getArkivId());

		JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, hentForsendelseResponseTo
				.getBestillingsId());
		String arkivmeldingXmlString = marshalArkivmeldingToXmlString(arkivmeldingJAXBElement);

		integrasjonspunkt.opprettMelding(createMessageRequestMapper.map(conversationId, hentForsendelseResponseTo), conversationId);
		dokdistDokumentList.forEach(dokdistDokument -> integrasjonspunkt.lastOppFil(dokdistDokument, conversationId));
		integrasjonspunkt.sendMelding(conversationId);

		juridiskLogg.lagreJuridiskLogg(lagreJuridiskLoggMapper.map(hentForsendelseResponseTo, arkivmeldingXmlString.getBytes()));
	}

	private List<DokdistDokument> getDocumentsFromS3(HentForsendelseResponseTo hentForsendelseResponseTo) {
		return hentForsendelseResponseTo.getDokumenter().stream()
				.map(dokumentTo -> {
					String jsonPayload = s3Storage.get(dokumentTo.getDokumentObjektReferanse());
					DokdistDokument dokdistDokument = deserializeS3JsonPayloadToDokdistDokument(jsonPayload, dokumentTo.getDokumentObjektReferanse());
					dokdistDokument.setDokumentInfoId(dokumentTo.getArkivDokumentInfoId());
					return dokdistDokument;
				})
				.collect(Collectors.toList());
	}

	private String marshalArkivmeldingToXmlString(JAXBElement<Arkivmelding> arkivmeldingJAXBElement) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Arkivmelding.class);
			Marshaller marshaller = jaxbContext.createMarshaller();

			StringWriter sw = new StringWriter();
			marshaller.marshal(arkivmeldingJAXBElement, sw);
			return sw.toString();
		} catch (JAXBException e) {
			throw new KunneIkkeMarshalleArkivmeldingTechnicalException("Kunne ikke marshalle Arkivmelding til xmlString", e);
		}
	}
}
