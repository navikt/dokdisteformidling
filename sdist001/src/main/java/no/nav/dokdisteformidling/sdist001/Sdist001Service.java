package no.nav.dokdisteformidling.sdist001;

import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_BEKREFTET;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_EKSPEDERT;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_FEILET;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_OVERSENDT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.integrasjonspunkt.Integrasjonspunkt;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.juridisklogg.LagreJuridiskLoggMapper;
import no.nav.dokdisteformidling.consumer.juridisklogg.LoggMeldingRequest;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentEformidlingforsendelserResponseTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.exception.functional.KunneIkkeSerialisereEformidlingstatusoppdateringTilJson;
import no.nav.dokdisteformidling.sdist001.domain.EformidlingStatusOppdatering;
import no.nav.dokdisteformidling.sdist001.domain.ForsendelseStatusEndringer;
import org.springframework.stereotype.Service;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Slf4j
@Service
public class Sdist001Service {
	public static final String KONVERSASJON_STATUS_LEST = "LEST";
	public static final String KONVERSASJON_STATUS_LEVERT = "LEVERT";
	public static final String KONVERSASJON_STATUS_MOTTATT = "MOTTATT";
	public static final String KONVERSASJON_STATUS_OPPRETTET = "OPPRETTET";
	public static final String KONVERSASJON_STATUS_SENDT = "SENDT";
	public static final String KONVERSASJON_STATUS_LEVETID_UTLOPT = "LEVETID_UTLOPT";

	private final AdministrerForsendelse administrerForsendelse;
	private final Integrasjonspunkt integrasjonspunkt;
	private final JuridiskLogg juridiskLogg;
	private final LagreJuridiskLoggMapper lagreJuridiskLoggMapper;
	private final EformidlingStatusOppdateringMapper eformidlingStatusOppdateringMapper;

	public Sdist001Service(AdministrerForsendelse administrerForsendelse,
						   Integrasjonspunkt integrasjonspunkt,
						   JuridiskLogg juridiskLogg,
						   LagreJuridiskLoggMapper lagreJuridiskLoggMapper,
						   EformidlingStatusOppdateringMapper eformidlingStatusOppdateringMapper) {
		this.administrerForsendelse = administrerForsendelse;
		this.integrasjonspunkt = integrasjonspunkt;
		this.juridiskLogg = juridiskLogg;
		this.lagreJuridiskLoggMapper = lagreJuridiskLoggMapper;
		this.eformidlingStatusOppdateringMapper = eformidlingStatusOppdateringMapper;
	}

	public void kontrollerOgOppdaterStatus(HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelseTo,
										   ForsendelseStatusEndringer forsendelseStatusEndringer) {
		String forsendelseStatus = forsendelseTo.getForsendelseStatus();
		String forsendelseId = forsendelseTo.getForsendelseId();

		if (!FORSENDELSE_STATUS_OVERSENDT.equals(forsendelseStatus) && !FORSENDELSE_STATUS_BEKREFTET.equals(forsendelseStatus)) {
			log.warn("ForsendelseId={} med status={} ble feilaktig returnert av hentEformidlingForsendelser.", forsendelseId, forsendelseStatus);
			return;
		}

		String konversasjonId = forsendelseTo.getKonversasjonId();
		String integrasjonspunktStatus = integrasjonspunkt.getStatus(konversasjonId);
		integrasjonspunktStatus = integrasjonspunktStatus == null ? "null" : integrasjonspunktStatus;

		if (FORSENDELSE_STATUS_OVERSENDT.equals(forsendelseStatus)) {
			kontrollerStatusOversendt(integrasjonspunktStatus, forsendelseId, konversasjonId, forsendelseStatusEndringer);
		} else {
			kontrollerStatusBekreftet(integrasjonspunktStatus, forsendelseId, konversasjonId, forsendelseStatusEndringer);
		}
	}

	private void kontrollerStatusOversendt(String integrasjonspunktStatus, String forsendelseId, String konversasjonId,
										   ForsendelseStatusEndringer forsendelseStatusEndringer) {
		switch (integrasjonspunktStatus) {
			case KONVERSASJON_STATUS_LEVETID_UTLOPT:
				log.error("Avvik har oppstått for forsendelseId={}. Forsendelsen settes til FEILET.", forsendelseId);
				administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FORSENDELSE_STATUS_FEILET);
				forsendelseStatusEndringer.getFeilet().add(forsendelseId);
				break;
			case KONVERSASJON_STATUS_OPPRETTET:
				// ingen endring
				break;
			case KONVERSASJON_STATUS_SENDT:
				administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FORSENDELSE_STATUS_BEKREFTET);
				forsendelseStatusEndringer.getBekreftet().add(forsendelseId);
				break;
			case KONVERSASJON_STATUS_MOTTATT:
			case KONVERSASJON_STATUS_LEVERT:
			case KONVERSASJON_STATUS_LEST:
				oppdaterTilEkspedert(integrasjonspunktStatus, forsendelseId, konversasjonId);
				forsendelseStatusEndringer.getEkspedert().add(forsendelseId);
				break;
			default:
				log.warn("Uventet status={} fra integrasjonspunkt for forsendelseId={} med forsendelseStatus={}.",
						integrasjonspunktStatus, forsendelseId, FORSENDELSE_STATUS_OVERSENDT);
				break;
		}
	}

	private void kontrollerStatusBekreftet(String integrasjonspunktStatus, String forsendelseId, String konversasjonId,
										   ForsendelseStatusEndringer forsendelseStatusEndringer) {
		switch (integrasjonspunktStatus) {
			case KONVERSASJON_STATUS_LEVETID_UTLOPT:
				log.error("Avvik har oppstått for forsendelseId={}. Forsendelsen settes til FEILET.", forsendelseId);
				administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FORSENDELSE_STATUS_FEILET);
				forsendelseStatusEndringer.getFeilet().add(forsendelseId);
				break;
			case KONVERSASJON_STATUS_SENDT:
				// ingen endring
				break;
			case KONVERSASJON_STATUS_MOTTATT:
			case KONVERSASJON_STATUS_LEVERT:
			case KONVERSASJON_STATUS_LEST:
				oppdaterTilEkspedert(integrasjonspunktStatus, forsendelseId, konversasjonId);
				forsendelseStatusEndringer.getEkspedert().add(forsendelseId);
				break;
			default:
				log.warn("Uventet status={} fra integrasjonspunkt for forsendelseId={} med forsendelseStatus={}.",
						integrasjonspunktStatus, forsendelseId, FORSENDELSE_STATUS_BEKREFTET);
				break;
		}
	}

	private void oppdaterTilEkspedert(String integrasjonspunktStatus, String forsendelseId, String konversasjonId) {
		HentForsendelseResponseTo hentForsendelseResponseTo = administrerForsendelse.hentForsendelse(forsendelseId);
		EformidlingStatusOppdatering eformidlingStatusOppdatering =
				eformidlingStatusOppdateringMapper.map(konversasjonId, integrasjonspunktStatus);

		try {
			byte[] meldingsInnhold = new ObjectMapper().writeValueAsBytes(eformidlingStatusOppdatering);
			LoggMeldingRequest loggMeldingRequest = lagreJuridiskLoggMapper.map(hentForsendelseResponseTo, meldingsInnhold);
			juridiskLogg.lagreJuridiskLogg(loggMeldingRequest);
		} catch (JsonProcessingException e) {
			throw new KunneIkkeSerialisereEformidlingstatusoppdateringTilJson(
					"Kunne ikke serialisere eformidlingstatusoppdatering til JSON.", e);
		}

		administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FORSENDELSE_STATUS_EKSPEDERT);
	}
}
