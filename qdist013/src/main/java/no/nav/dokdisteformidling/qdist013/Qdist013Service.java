package no.nav.dokdisteformidling.qdist013;

import static no.nav.dokdisteformidling.common.FunctionalUtils.deserializeS3JsonPayloadToDokdistDokument;
import static no.nav.dokdisteformidling.common.FunctionalUtils.validateThatForsendelseStatusIsKlarForDist;

import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.qdist013.saf.JournalpostQdist013;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.S3Storage;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class Qdist013Service {

	private final S3Storage s3Storage;
	private final AdministrerForsendelse administrerForsendelse;
	private final SafJournalpostQueryService safJournalpostQueryService;
	private final JuridiskLogg juridiskLogg;
	private final LagreJuridiskLoggMapper lagreJuridiskLoggMapper;

	public Qdist013Service(S3Storage s3Storage,
						   AdministrerForsendelse administrerForsendelse,
						   @Qualifier("SafJournalpostQueryServiceQdist013") SafJournalpostQueryService safJournalpostQueryService,
						   JuridiskLogg juridiskLogg,
						   LagreJuridiskLoggMapper lagreJuridiskLoggMapper) {
		this.s3Storage = s3Storage;
		this.administrerForsendelse = administrerForsendelse;
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.juridiskLogg = juridiskLogg;
		this.lagreJuridiskLoggMapper = lagreJuridiskLoggMapper;
	}

	@Handler
	public void processForsendelse(DistribuerForsendelseTilTrygderetten distribuerForsendelseTilTrygderetten) {
		final HentForsendelseResponseTo hentForsendelseResponseTo = administrerForsendelse.hentForsendelse(distribuerForsendelseTilTrygderetten
				.getForsendelseId());
		validateThatForsendelseStatusIsKlarForDist(hentForsendelseResponseTo.getForsendelseStatus());

		final List<DokdistDokument> dokdistDokumentList = getDocumentsFromS3(hentForsendelseResponseTo);

		final JournalpostQdist013 journalpostQdist013 = safJournalpostQueryService.hentJournalpost(hentForsendelseResponseTo.getArkivInformasjon()
				.getArkivId());

		//TODO produser arkivmelding og send til trygderetten gjennom restkall

		juridiskLogg.lagreJuridiskLogg(lagreJuridiskLoggMapper.map(hentForsendelseResponseTo, "Implement me".getBytes())); //todo: meldingsInnhold = arkivmeldingen, men ikke dokumentene
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
}
