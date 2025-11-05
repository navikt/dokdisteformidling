package no.nav.dokdisteformidling.qdist013;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.eformidling.Eformidling;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokumentpakke;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.juridisklogg.LagreJuridiskLoggMapper;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponse;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.exception.functional.InvalidForsendelseStatusFunctionalException;
import no.nav.dokdisteformidling.exception.functional.KunneIkkeDeserialisereBucketJsonPayloadFunctionalException;
import no.nav.dokdisteformidling.qdist013.avtaltmelding.Avtaltmelding;
import no.nav.dokdisteformidling.qdist013.avtaltmelding.AvtaltmeldingService;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import no.nav.dokdisteformidling.storage.BucketStorage;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_KLAR_FOR_DIST;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_ANTALL_DOK;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_BESTILLINGS_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_CONVERSATION_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_JOURNALPOST_ID;
import static no.nav.dokdisteformidling.consumer.eformidling.NavDokument.fromAvtaltmelding;
import static no.nav.dokdisteformidling.consumer.eformidling.NavDokument.fromVedlegg;

@Slf4j
@Service
public class Qdist013Service {

	private final BucketStorage bucketStorage;
	private final AdministrerForsendelse administrerForsendelse;
	private final SafJournalpostQueryService<JournalpostQdist013> safJournalpostQueryService;
	private final JuridiskLogg juridiskLogg;
	private final LagreJuridiskLoggMapper lagreJuridiskLoggMapper;
	private final AvtaltmeldingService avtaltmeldingService;
	private final Eformidling eformidling;

	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");


	public Qdist013Service(BucketStorage bucketStorage,
						   AdministrerForsendelse administrerForsendelse,
						   @Qualifier("SafJournalpostQueryServiceQdist013") SafJournalpostQueryService<JournalpostQdist013> safJournalpostQueryService,
						   JuridiskLogg juridiskLogg,
						   LagreJuridiskLoggMapper lagreJuridiskLoggMapper,
						   AvtaltmeldingService avtaltmeldingService,
						   Eformidling eformidling) {
		this.bucketStorage = bucketStorage;
		this.administrerForsendelse = administrerForsendelse;
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.juridiskLogg = juridiskLogg;
		this.lagreJuridiskLoggMapper = lagreJuridiskLoggMapper;
		this.avtaltmeldingService = avtaltmeldingService;
		this.eformidling = eformidling;
	}

	@Handler
	public void processForsendelse(DistribuerForsendelseTilTrygderetten distribuerForsendelseTilTrygderetten, Exchange exchange) {
		final String conversationId = UUID.randomUUID().toString();
		exchange.setProperty(PROPERTY_CONVERSATION_ID, conversationId);

		final Long forsendelseId = Long.valueOf(distribuerForsendelseTilTrygderetten.forsendelseId());
		final HentForsendelseResponse hentForsendelseResponse = administrerForsendelse.hentForsendelse(forsendelseId);

		final String bestillingsId = hentForsendelseResponse.getBestillingsId();
		exchange.setProperty(PROPERTY_BESTILLINGS_ID, bestillingsId);

		final String journalpostId = hentForsendelseResponse.getArkivInformasjon().getArkivId();
		exchange.setProperty(PROPERTY_JOURNALPOST_ID, journalpostId);
		exchange.setProperty(PROPERTY_CONVERSATION_ID, conversationId);
		validateThatForsendelseStatusIsKlarForDist(hentForsendelseResponse.getForsendelseStatus());

		final List<DokdistDokument> dokdistDokumentList = getDocumentsFromBucket(hentForsendelseResponse);
		exchange.setProperty(PROPERTY_ANTALL_DOK, dokdistDokumentList.size());
		final JournalpostQdist013 journalpostQdist013 = safJournalpostQueryService.hentJournalpost(hentForsendelseResponse.getArkivInformasjon()
				.getArkivId());
		final Avtaltmelding avtaltmelding = avtaltmeldingService.map(journalpostQdist013, bestillingsId);
		final byte[] avtaltmeldingBytes = avtaltmelding.asBytes();

		log.info("Sender eformidling forsendelse direkte til Altinn formidlingstjenesten. forsendelseId={}, konversasjonsId={}, bestillingsId={}",
				forsendelseId, conversationId, bestillingsId);

		final int avtaltmeldingMedian = avtaltmelding.asXmlString().length()/2;
		log.info("Avtaltmelding {}", avtaltmelding.asXmlString().substring(0, avtaltmeldingMedian));
		log.info("Avtaltmelding {}", avtaltmelding.asXmlString().substring(avtaltmeldingMedian, avtaltmelding.asXmlString().length()-1));

		eformidling.send(NavDokumentpakke.builder()
				.conversationId(conversationId)
				.bestillingsId(bestillingsId)
				.messageChannelInstanceIdentifier(UUID.randomUUID())
				.arkivmelding(fromAvtaltmelding(new ByteArrayInputStream(avtaltmeldingBytes)))
				.navDokumenter(dokdistDokumentList.stream()
						.map(d -> fromVedlegg(avtaltmelding.lookupFilnavn(d.getDokumentInfoId()),
								new ByteArrayInputStream(d.getPdf())))
						.collect(Collectors.toList()))
				.build(), avtaltmelding.asXmlString());

		juridiskLogg.lagreJuridiskLogg(lagreJuridiskLoggMapper.map(hentForsendelseResponse, avtaltmeldingBytes));
	}

	private List<DokdistDokument> getDocumentsFromBucket(HentForsendelseResponse hentForsendelseResponse) {
		return hentForsendelseResponse.getDokumenter().stream()
				.map(dokumentTo -> {
					String jsonPayload = bucketStorage.downloadObject(dokumentTo.getDokumentObjektReferanse(), hentForsendelseResponse.getBestillingsId());
					DokdistDokument dokdistDokument = deserializeBucketJsonPayloadToDokdistDokument(jsonPayload, dokumentTo.getDokumentObjektReferanse());
					dokdistDokument.setDokumentInfoId(dokumentTo.getArkivDokumentInfoId());
					return dokdistDokument;
				})
				.collect(Collectors.toList());
	}

	private static void validateThatForsendelseStatusIsKlarForDist(String forsendelseStatus) {
		if (!FORSENDELSE_STATUS_KLAR_FOR_DIST.equals(forsendelseStatus)) {
			throw new InvalidForsendelseStatusFunctionalException(
					format("ForsendelseStatus må være %s. Fant forsendelseStatus=%s", FORSENDELSE_STATUS_KLAR_FOR_DIST, forsendelseStatus)
			);
		}
	}

	private static DokdistDokument deserializeBucketJsonPayloadToDokdistDokument(String jsonPayload, String objektReferanse) {
		try {
			DokdistDokument dokdistDokument = JsonSerializer.deserialize(jsonPayload, DokdistDokument.class);
			dokdistDokument.setDokumentObjektReferanse(objektReferanse);
			return dokdistDokument;
		} catch (IllegalStateException e) {
			throw new KunneIkkeDeserialisereBucketJsonPayloadFunctionalException(format("Kunne ikke deserialisere jsonPayload fra bucket for dokument med dokumentobjektreferanse=%s. Dokumentet er ikke persistert til bucket med korrekt format!", objektReferanse));
		}
	}
}
