package no.nav.dokdisteformidling.qdist013;

import lombok.extern.slf4j.Slf4j;
import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivmelding.Journalpost;
import no.nav.dokdisteformidling.config.props.FeatureToggleProperties;
import no.nav.dokdisteformidling.consumer.eformidling.Eformidling;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokumentpakke;
import no.nav.dokdisteformidling.consumer.integrasjonspunkt.CreateMessageRequest;
import no.nav.dokdisteformidling.consumer.integrasjonspunkt.Integrasjonspunkt;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.juridisklogg.LagreJuridiskLoggMapper;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.exception.functional.IkkeSammenfallendeIderFunctionalException;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeMarshalleArkivmeldingTechnicalException;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.Storage;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.common.FunctionalUtils.deserializeS3JsonPayloadToDokdistDokument;
import static no.nav.dokdisteformidling.common.FunctionalUtils.generateRandomUUID;
import static no.nav.dokdisteformidling.common.FunctionalUtils.validateThatForsendelseStatusIsKlarForDist;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_ANTALL_DOK;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_BESTILLINGS_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_CONVERSATION_ID;
import static no.nav.dokdisteformidling.consumer.eformidling.NavDokument.fromArkivmelding;
import static no.nav.dokdisteformidling.consumer.eformidling.NavDokument.fromVedlegg;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Service
public class Qdist013Service {

	public static final String ARKIVMELDING = "arkivmelding";
	public static final String ARKIVMELDING_XML = ARKIVMELDING + ".xml";

	private final Storage s3Storage;
	private final AdministrerForsendelse administrerForsendelse;
	private final SafJournalpostQueryService<JournalpostQdist013> safJournalpostQueryService;
	private final JuridiskLogg juridiskLogg;
	private final Integrasjonspunkt integrasjonspunkt;
	private final LagreJuridiskLoggMapper lagreJuridiskLoggMapper;
	private final ArkivmeldingMapper arkivmeldingMapper;
	private final CreateMessageRequestMapper createMessageRequestMapper;
	private final FeatureToggleProperties featureToggleProperties;
	private final Eformidling eformidling;

	public Qdist013Service(Storage s3Storage,
						   AdministrerForsendelse administrerForsendelse,
						   @Named("SafJournalpostQueryServiceQdist013") SafJournalpostQueryService<JournalpostQdist013> safJournalpostQueryService,
						   JuridiskLogg juridiskLogg,
						   Integrasjonspunkt integrasjonspunkt,
						   ArkivmeldingMapper arkivmeldingMapper,
						   CreateMessageRequestMapper createMessageRequestMapper,
						   FeatureToggleProperties featureToggleProperties,
						   Eformidling eformidling) {
		this.s3Storage = s3Storage;
		this.administrerForsendelse = administrerForsendelse;
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.juridiskLogg = juridiskLogg;
		this.integrasjonspunkt = integrasjonspunkt;
		this.lagreJuridiskLoggMapper = new LagreJuridiskLoggMapper();
		this.arkivmeldingMapper = arkivmeldingMapper;
		this.createMessageRequestMapper = createMessageRequestMapper;
		this.featureToggleProperties = featureToggleProperties;
		this.eformidling = eformidling;
	}

	@Handler
	public void processForsendelse(DistribuerForsendelseTilTrygderetten distribuerForsendelseTilTrygderetten, Exchange exchange) {
		final String conversationId = generateRandomUUID(); //
		exchange.setProperty(PROPERTY_CONVERSATION_ID, conversationId);
		final String forsendelseId = distribuerForsendelseTilTrygderetten.getForsendelseId();
		final HentForsendelseResponseTo hentForsendelseResponseTo = administrerForsendelse.hentForsendelse(forsendelseId);
		final String bestillingsId = hentForsendelseResponseTo.getBestillingsId();
		exchange.setProperty(PROPERTY_BESTILLINGS_ID, bestillingsId);
		validateThatForsendelseStatusIsKlarForDist(hentForsendelseResponseTo.getForsendelseStatus());

		final List<DokdistDokument> dokdistDokumentList = getDocumentsFromS3(hentForsendelseResponseTo);
		exchange.setProperty(PROPERTY_ANTALL_DOK, dokdistDokumentList.size());
		final JournalpostQdist013 journalpostQdist013 = safJournalpostQueryService.hentJournalpost(hentForsendelseResponseTo.getArkivInformasjon()
				.getArkivId());
		final JAXBElement<Arkivmelding> arkivmeldingJAXBElement = arkivmeldingMapper.createArkivMelding(journalpostQdist013, bestillingsId);
		final String arkivmeldingXmlString = marshalArkivmeldingToXmlString(arkivmeldingJAXBElement);
		final CreateMessageRequest createMessageRequest = createMessageRequestMapper.map(conversationId, hentForsendelseResponseTo);

		if (featureToggleProperties.isUsealtinnformidlingstjenesten()) {
			log.info("Sender eformidling forsendelse direkte til Altinn formidlingstjenesten. forsendelseId={}, konversasjonsId={}, bestillingsId={}",
					forsendelseId, conversationId, bestillingsId);
			eformidling.send(NavDokumentpakke.builder()
					.conversationId(conversationId)
					.bestillingsId(bestillingsId)
					.arkivmelding(fromArkivmelding(new ByteArrayInputStream(arkivmeldingXmlString.getBytes(StandardCharsets.UTF_8))))
					.navDokumenter(dokdistDokumentList.stream()
							.map(d -> fromVedlegg(getDocumentFilename(arkivmeldingJAXBElement.getValue(), journalpostQdist013.getJournalpostId(), d.getDokumentInfoId()),
									new ByteArrayInputStream(d.getPdf())))
							.collect(Collectors.toList()))
					.build());
			juridiskLogg.lagreJuridiskLogg(lagreJuridiskLoggMapper.map(hentForsendelseResponseTo, arkivmeldingXmlString.getBytes()));
		} else {
			log.info("Sender eformidling forsendelse til integrasjonspunktet til NAV. forsendelseId={}, konversasjonsId={}, bestillingsId={}",
					forsendelseId, conversationId, bestillingsId);
			integrasjonspunkt.opprettMelding(createMessageRequest, conversationId);
			uploadDocuments(dokdistDokumentList, arkivmeldingJAXBElement.getValue(), journalpostQdist013.getJournalpostId(), bestillingsId);
			uploadArkivmelding(arkivmeldingXmlString, bestillingsId);
			integrasjonspunkt.sendMelding(bestillingsId);
			juridiskLogg.lagreJuridiskLogg(lagreJuridiskLoggMapper.map(hentForsendelseResponseTo, arkivmeldingXmlString.getBytes()));
		}
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

	private void uploadDocuments(List<DokdistDokument> dokdistDokumentList, Arkivmelding arkivmelding, String journalpostId, String bestillingsId) {
		dokdistDokumentList.forEach(dokdistDokument -> {
			final String title = getDocumentTitle(arkivmelding, journalpostId, dokdistDokument.getDokumentInfoId());
			final String filename = getDocumentFilename(arkivmelding, journalpostId, dokdistDokument.getDokumentInfoId());
			integrasjonspunkt.lastOppFil(dokdistDokument, title, filename, bestillingsId);
		});
	}

	private void uploadArkivmelding(String arkivmeldingXmlString, String bestillingsId) {
		integrasjonspunkt.lastOppFil(DokdistDokument.builder()
				.pdf(arkivmeldingXmlString.getBytes())
				.build(), ARKIVMELDING, ARKIVMELDING_XML, bestillingsId);
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

	private String getDocumentTitle(Arkivmelding arkivmelding, String journalpostId, String dokumentInfoId) {
		Dokumentbeskrivelse dokumentbeskrivelse = getDokumentbeskrivelseByJpIdAndDokInfoId(arkivmelding, journalpostId, dokumentInfoId);
		return dokumentbeskrivelse.getTittel();
	}

	private String getDocumentFilename(Arkivmelding arkivmelding, String journalpostId, String dokumentInfoId) {
		Dokumentbeskrivelse dokumentbeskrivelse = getDokumentbeskrivelseByJpIdAndDokInfoId(arkivmelding, journalpostId, dokumentInfoId);
		return dokumentbeskrivelse.getDokumentobjekt().get(0).getReferanseDokumentfil();

	}

	private Dokumentbeskrivelse getDokumentbeskrivelseByJpIdAndDokInfoId(Arkivmelding arkivmelding, String journalpostId, String dokumentInfoId) {
		Journalpost journalpost = (Journalpost) arkivmelding.getMappe().get(0).getBasisregistrering().get(0);
		return (Dokumentbeskrivelse) journalpost.getDokumentbeskrivelseAndDokumentobjekt()
				.stream()
				.filter(dokumentbeskrivelse -> ((Dokumentbeskrivelse) dokumentbeskrivelse).getDokumentobjekt()
						.get(0).getReferanseDokumentfil().startsWith(format("%s-%s", journalpostId, dokumentInfoId)))
				.findAny()
				.orElseThrow(() -> new IkkeSammenfallendeIderFunctionalException(format("DokumentInfoId=%s finnes på foresendelsen i dokdistDb, men ikke i respons fra SAF på journalpostId=%s.", dokumentInfoId, journalpostId)));
	}
}
