package no.nav.dokdisteformidling.sdist001;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.eformidling.Eformidling;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.juridisklogg.LagreJuridiskLoggMapper;
import no.nav.dokdisteformidling.consumer.juridisklogg.LoggMeldingRequest;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentEformidlingforsendelserResponseTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.exception.functional.KunneIkkeSerialisereEformidlingstatusoppdateringTilJson;
import no.nav.dokdisteformidling.metrics.Monitor;
import no.nav.dokdisteformidling.sdist001.domain.EformidlingStatusOppdatering;
import no.nav.dokdisteformidling.sdist001.domain.ForsendelseStatusEndringer;
import no.nav.dokdisteformidling.sdist001.domain.to.AltinnKvitteringStatus;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final ForsendelseStatusEndringer forsendelseStatusEndringer;

    public Sdist001Service(AdministrerForsendelse administrerForsendelse,
                           JuridiskLogg juridiskLogg,
                           Eformidling eformidling,
                           LagreJuridiskLoggMapper lagreJuridiskLoggMapper) {
        this.administrerForsendelse = administrerForsendelse;
        this.juridiskLogg = juridiskLogg;
        this.lagreJuridiskLoggMapper = lagreJuridiskLoggMapper;
        this.eformidlingStatusOppdateringMapper = new EformidlingStatusOppdateringMapper();
        this.eformidling = eformidling;
        this.forsendelseStatusEndringer = new ForsendelseStatusEndringer();
    }

    @Monitor(value = "dok_metric", extraTags = {"process", "oppdatertDokDistEformidlingStatus"}, histogram = true)
    public void oppdatereDokDistEformidlingStatus() {
        forsendelseStatusEndringer.clear();
        List<HentEformidlingforsendelserResponseTo.ForsendelseTo> forsendelserTo = administrerForsendelse.hentEformidlingForsendelser().getForsendelser();
        log.info("Hentet eformidlingforsendelser fra rdist001 {} ", forsendelserTo);
        eformidling.hent()
                .forEach(downloadResponse -> {
                    log.info(String.format("Hentet trygderetten kvittering melding fra Altinn med konversasjonId=%s, SendersReference=%s, KvitteringStatus=%s",
                            downloadResponse.getConversationId(), downloadResponse.getSendersReference(), downloadResponse.getKvitteringStatus()));
                    forsendelserTo.stream()
                            .filter(forsendelseTo -> downloadResponse.getConversationId().equals(forsendelseTo.getKonversasjonId()))
                            .filter(forsendelse -> !EKSPEDERT.name().equals(forsendelse.getForsendelseStatus()))
                            .forEach(forsendelse -> {
                                log.info(String.format("Sdist001 har mottatt kall til å oppdatere  forsendelse med forsendelseId:%s, konversasjonId=%s",
                                        forsendelse.getForsendelseId(), forsendelse.getKonversasjonId()));
                                kontrollerEformidlingStatus(downloadResponse.getKvitteringStatus().getStatus(), forsendelse);
                                eformidling.bekreft(downloadResponse.getFileReference());
                            });
                });
        log.info("sdist001 har oppdatert status for eFormidlingforsendelser: {}", forsendelseStatusEndringer.toString());

    }

    public void kontrollerEformidlingStatus(String kvitteringStatus, HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelseTo) {
        String forsendelseStatus = forsendelseTo.getForsendelseStatus();
        String forsendelseId = forsendelseTo.getForsendelseId();
        String konversasjonId = forsendelseTo.getKonversasjonId();

        if (!OVERSENDT.name().equals(forsendelseStatus) && !BEKREFTET.name().equals(forsendelseStatus)) {
            log.warn("ForsendelseId={} med status={} ble feilaktig returnert av hentEformidlingForsendelser.", forsendelseId, forsendelseStatus);
            return;
        }
        log.info("Sdist001 har mottatt kall med {}", forsendelseTo);

        try {
            oppdaterEformidlingStatus(kvitteringStatus, konversasjonId, forsendelseId, forsendelseStatus);
        } catch (Exception e) {
            log.error("sdist001 feilet under oppdatering av status for eFormidlingforsendelser: " + e.getMessage(), e);
            return;
        }

    }

    private void oppdaterEformidlingStatus(String kvitteringStatus, String konversasjonId, String forsendelseId, String forsendelseStatus) {
        AltinnKvitteringStatus altinnKvitteringStatus = AltinnKvitteringStatus.valueOf(kvitteringStatus);
        switch (altinnKvitteringStatus) {
            case SENDT:
                administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, BEKREFTET.name());
                forsendelseStatusEndringer.getBekreftet().add(forsendelseId);
                break;
            case MOTTATT:
                log.info("Hentet eFormidlingforsendelser status fra Altinn med kvitteringStatus={}, konversasjonId={}", kvitteringStatus, konversasjonId);
                break;
            case LEVERT:
            case LEST:
                oppdaterTilEkspedert(altinnKvitteringStatus.name(), forsendelseId, konversasjonId);
                forsendelseStatusEndringer.getEkspedert().add(forsendelseId);
                break;
            case FAIL:
                break;
            case LEVETID_UTLOPT:
                log.error("Avvik har oppstått for forsendelseId={}, konversasjonId={}. Forsendelsen settes til FEILET.", forsendelseId, konversasjonId);
                administrerForsendelse.oppdaterForsendelseStatus(forsendelseId, FEIL.name());
                forsendelseStatusEndringer.getFeilet().add(forsendelseId);
                break;
            default:
                log.warn("Uventet status={} fra Altinn brokerservice for forsendelseId={} med forsendelseStatus={}.",
                        altinnKvitteringStatus, forsendelseId, forsendelseStatus);
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

