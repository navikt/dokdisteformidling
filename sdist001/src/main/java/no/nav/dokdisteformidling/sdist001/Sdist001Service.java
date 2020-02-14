package no.nav.dokdisteformidling.sdist001;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.eformidling.Eformidling;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.KvitteringStatus;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.juridisklogg.LagreJuridiskLoggMapper;
import no.nav.dokdisteformidling.consumer.juridisklogg.LoggMeldingRequest;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentEformidlingforsendelserResponseTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.exception.functional.KunneIkkeSerialisereEformidlingstatusoppdateringTilJson;
import no.nav.dokdisteformidling.exception.technical.FantIkkeKvitteringFraAltinn;
import no.nav.dokdisteformidling.sdist001.domain.EformidlingStatusOppdatering;
import no.nav.dokdisteformidling.sdist001.domain.ForsendelseStatusEndringer;
import no.nav.dokdisteformidling.sdist001.domain.to.AltinnKvitteringStatus;
import org.springframework.stereotype.Service;

import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.BEKREFTET;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.EKSPEDERT;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.FEIL;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.OVERSENDT;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Slf4j
@Service
public class Sdist001Service {

    private final AdministrerForsendelse administrerForsendelse;
    private final Eformidling eformidling;
    private final JuridiskLogg juridiskLogg;
    private final LagreJuridiskLoggMapper lagreJuridiskLoggMapper;
    private final EformidlingStatusOppdateringMapper eformidlingStatusOppdateringMapper;
    private AltinnKvitteringStatus altinnKvitteringStatus;

    public Sdist001Service(AdministrerForsendelse administrerForsendelse,
                           JuridiskLogg juridiskLogg,
                           Eformidling eformidling) {
        this.administrerForsendelse = administrerForsendelse;
        this.juridiskLogg = juridiskLogg;
        this.lagreJuridiskLoggMapper = new LagreJuridiskLoggMapper();
        this.eformidlingStatusOppdateringMapper = new EformidlingStatusOppdateringMapper();
        this.eformidling = eformidling;
    }

    public void kontrollerOgOppdaterStatus(HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelseTo,
                                           ForsendelseStatusEndringer forsendelseStatusEndringer) {
        String forsendelseStatus = forsendelseTo.getForsendelseStatus();
        String forsendelseId = forsendelseTo.getForsendelseId();

        if (!OVERSENDT.name().equals(forsendelseStatus) && !BEKREFTET.name().equals(forsendelseStatus)) {
            log.warn("ForsendelseId={} med status={} ble feilaktig returnert av hentEformidlingForsendelser.", forsendelseId, forsendelseStatus);
            return;
        }
        String konversasjonId = forsendelseTo.getKonversasjonId();
        log.info( String.format("Sdist001 kovnersajonId=%s og forsendelseId=%s", konversasjonId,forsendelseId));

        KvitteringStatus altinnKvitteringStatus = eformidling.hent().stream()
                .filter(downloadResponse -> konversasjonId.equals(downloadResponse.getConversationId()))
                .map(downloadResponse ->  {
                    log.info(String.format("Hentet kvittering status fra Altinn med konversasjonId=%s, SendersReference=%s,KvitteringStatus=%s",
                            downloadResponse.getConversationId(),downloadResponse.getSendersReference(),downloadResponse.getKvitteringStatus()));
                   return downloadResponse.getKvitteringStatus();
                })
                .findAny()
                .orElseThrow(()-> new FantIkkeKvitteringFraAltinn("Fant ikke kvittering status fra altinn"));

        if (OVERSENDT.name().equals(forsendelseStatus)) {
            kontrollerStatusOversendt(altinnKvitteringStatus.getStatus(), forsendelseId, konversasjonId, forsendelseStatusEndringer);
        } else {
            kontrollerStatusBekreftet(altinnKvitteringStatus.getStatus(), forsendelseId, konversasjonId, forsendelseStatusEndringer);
        }
    }

    private void kontrollerStatusOversendt(String kvitteringStatus, String forsendelseId, String konversasjonId,
                                           ForsendelseStatusEndringer forsendelseStatusEndringer) {
        altinnKvitteringStatus = AltinnKvitteringStatus.valueOf(kvitteringStatus);
        switch (altinnKvitteringStatus) {
            case SENDT:
                administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, BEKREFTET.name());
                forsendelseStatusEndringer.getBekreftet().add(forsendelseId);
                break;
            case MOTTATT:
            case LEVERT:
            case LEST:
                oppdaterTilEkspedert(altinnKvitteringStatus.name(), forsendelseId, konversasjonId);
                forsendelseStatusEndringer.getEkspedert().add(forsendelseId);
                break;
            case FAIL:
                break;
            case LEVETID_UTLOPT:
                log.error("Avvik har oppstått for forsendelseId={}. Forsendelsen settes til FEILET.", forsendelseId);
                administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FEIL.name());
                forsendelseStatusEndringer.getFeilet().add(forsendelseId);
                break;
            default:
                log.warn("Uventet status={} fra Altinn brokerservice for forsendelseId={} med forsendelseStatus={}.",
                        altinnKvitteringStatus, forsendelseId, OVERSENDT);
                break;
        }

    }

    private void kontrollerStatusBekreftet(String kvitteringStatus, String forsendelseId, String konversasjonId,
                                           ForsendelseStatusEndringer forsendelseStatusEndringer) {

        altinnKvitteringStatus = AltinnKvitteringStatus.valueOf(kvitteringStatus);
        switch (altinnKvitteringStatus) {
            case SENDT:
            case MOTTATT:
            case LEVERT:
            case FAIL:
                break;
            case LEST:
                oppdaterTilEkspedert(kvitteringStatus, forsendelseId, konversasjonId);
                forsendelseStatusEndringer.getEkspedert().add(forsendelseId);
                break;
            case LEVETID_UTLOPT:
                log.error("Avvik har oppstått for forsendelseId={}. Forsendelsen settes til FEILET.", forsendelseId);
                administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FEIL.name());
                forsendelseStatusEndringer.getFeilet().add(forsendelseId);
                break;
            default:
                log.warn("Uventet status={} fra integrasjonspunkt for forsendelseId={} med forsendelseStatus={}.",
                        kvitteringStatus, forsendelseId, BEKREFTET.name());
                break;
        }

    }

    private void oppdaterTilEkspedert(String trygderettenKvitteringStatus, String forsendelseId, String konversasjonId) {
        HentForsendelseResponseTo hentForsendelseResponseTo = administrerForsendelse.hentForsendelse(forsendelseId);
        EformidlingStatusOppdatering eformidlingStatusOppdatering =
                eformidlingStatusOppdateringMapper.map(konversasjonId, trygderettenKvitteringStatus);

        try {
            byte[] meldingsInnhold = new ObjectMapper().writeValueAsBytes(eformidlingStatusOppdatering);
            LoggMeldingRequest loggMeldingRequest = lagreJuridiskLoggMapper.map(hentForsendelseResponseTo, meldingsInnhold);
            juridiskLogg.lagreJuridiskLogg(loggMeldingRequest);
        } catch (JsonProcessingException e) {
            throw new KunneIkkeSerialisereEformidlingstatusoppdateringTilJson(
                    "Kunne ikke serialisere eformidlingstatusoppdatering til JSON.", e);
        }

        administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, EKSPEDERT.name());
    }
}
