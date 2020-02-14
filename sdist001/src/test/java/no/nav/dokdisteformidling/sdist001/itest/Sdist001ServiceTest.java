package no.nav.dokdisteformidling.sdist001.itest;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokdisteformidling.consumer.eformidling.AltinnEformidling;
import no.nav.dokdisteformidling.consumer.eformidling.Eformidling;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.KvitteringStatus;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLoggConsumer;
import no.nav.dokdisteformidling.consumer.juridisklogg.LoggMeldingRequest;
import no.nav.dokdisteformidling.consumer.juridisklogg.LoggMeldingResponse;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdisteformidling.consumer.rdist001.HentEformidlingforsendelserResponseTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.sdist001.Sdist001Service;
import no.nav.dokdisteformidling.sdist001.domain.ForsendelseStatusEndringer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;

import static no.nav.dokdisteformidling.constants.DomainConstants.DEFAULT_ZONE_ID;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.BEKREFTET;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.OVERSENDT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class Sdist001ServiceTest {

    private final static String SENDERS_REFERENCE = "33259df3-18ae-45e6-9861-47f42e119a14";
    private final static String CONVERSATION_ID = "f1b3002b-1dea-4c14-8072-8c191183d04c";
    private static final String FIXED_TIME = "2020-01-01T12:00:00Z";
    private static final String FORSENDELSE_ID = "1234";
    private static final String KVITTERING_STATUS_MOTTATT = "MOTTATT";
    private static final String KVITTERING_STATUS_LEVERT = "LEVERT";
    public static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse(FIXED_TIME), DEFAULT_ZONE_ID);


    @Inject
    private Eformidling eformidling;

    @Inject
    private Sdist001Service sdist001Service;
    @Inject
    private AdministrerForsendelse administrerForsendelse;
    @Inject
    private JuridiskLogg juridiskLogg;

    private ForsendelseStatusEndringer forsendelseStatusEndringer;


    @BeforeEach
    public void setUp() {
        forsendelseStatusEndringer = new ForsendelseStatusEndringer();
        eformidling = mock(AltinnEformidling.class);
        juridiskLogg = mock(JuridiskLoggConsumer.class);
        administrerForsendelse = mock(AdministrerForsendelseConsumer.class);
        sdist001Service = new Sdist001Service(administrerForsendelse, juridiskLogg, eformidling);

    }


    @Test
    public void navFrosendelserShouldSetToExpedertWhenKvitteringStatusFraTrygdErMottatt() throws IOException, ClassNotFoundException {
        InputStream inputStream = new ClassPathResource("__files/rdist001/getForsendelse_forAltinnTest.json").getInputStream();

        HentForsendelseResponseTo forsendelseResponseTo = deserializeToObject(inputStream, HentForsendelseResponseTo.class);

        when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(hentEformidlingforsendelserResponseTo(OVERSENDT.name()));
        when(administrerForsendelse.hentForsendelse(FORSENDELSE_ID)).thenReturn(forsendelseResponseTo);
        when(eformidling.hent()).thenReturn(Collections.singletonList(getDownloadResponse(KVITTERING_STATUS_MOTTATT)));
        when(juridiskLogg.lagreJuridiskLogg(getLoggMeldingRequest())).thenReturn(getloggMeldingResponse());

        HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelseTo = hentEformidlingforsendelserResponseTo(OVERSENDT.name()).getForsendelser().get(0);

        sdist001Service.kontrollerOgOppdaterStatus(forsendelseTo, forsendelseStatusEndringer);

        verify(administrerForsendelse, never()).hentEformidlingForsendelser();
        verify(administrerForsendelse,times(1)).hentForsendelse(FORSENDELSE_ID);
        verify(eformidling, times(1)).hent();
        verify(juridiskLogg,times(1)).lagreJuridiskLogg(getLoggMeldingRequest());


    }

    @Test
    public void navFrosendelserShouldSetToExpedertWhenKvitteringStatusFraTrygdErLevert() throws IOException, ClassNotFoundException {
        InputStream inputStream = new ClassPathResource("__files/rdist001/getForsendelse_forAltinnTest.json").getInputStream();

        HentForsendelseResponseTo forsendelseResponseTo = deserializeToObject(inputStream, HentForsendelseResponseTo.class);

        when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(hentEformidlingforsendelserResponseTo(BEKREFTET.name()));
        when(administrerForsendelse.hentForsendelse(FORSENDELSE_ID)).thenReturn(forsendelseResponseTo);
        when(eformidling.hent()).thenReturn(Collections.singletonList(getDownloadResponse(KVITTERING_STATUS_LEVERT)));
        when(juridiskLogg.lagreJuridiskLogg(getLoggMeldingRequest())).thenReturn(getloggMeldingResponse());

        HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelseTo = hentEformidlingforsendelserResponseTo(BEKREFTET.name()).getForsendelser().get(0);

        sdist001Service.kontrollerOgOppdaterStatus(forsendelseTo, forsendelseStatusEndringer);

        verify(administrerForsendelse, never()).hentEformidlingForsendelser();
        verify(administrerForsendelse,times(1)).hentForsendelse(FORSENDELSE_ID);
        verify(eformidling, times(1)).hent();
        verify(juridiskLogg,times(1)).lagreJuridiskLogg(getLoggMeldingRequest());



    }


    public <T> T deserializeToObject(InputStream inputStream, Class<T> tClass) throws IOException, ClassNotFoundException {
        ObjectMapper objectMapper = new ObjectMapper();
        return (T) objectMapper.readValue(inputStream, tClass);


    }


    private DownloadResponse getDownloadResponse(String kvitteringStattus) {
        return DownloadResponse.builder()
                .conversationId(CONVERSATION_ID)
                .kvitteringStatus(KvitteringStatus.builder()
                        .status(kvitteringStattus)
                        .build())
                .sendersReference(SENDERS_REFERENCE)
                .sendtDate(LocalDateTime.now().toString())
                .build();
    }

    private HentEformidlingforsendelserResponseTo hentEformidlingforsendelserResponseTo(String forsendelseStatus) {

        return HentEformidlingforsendelserResponseTo.builder()
                .forsendelser(Collections.singletonList(HentEformidlingforsendelserResponseTo.ForsendelseTo.builder()
                        .distribusjonKanal("TRYGDERETTEN")
                        .forsendelseStatus("OVERSENDT")
                        .forsendelseId(FORSENDELSE_ID)
                        .konversasjonId(CONVERSATION_ID)
                        .build()))
                .build();

    }

    private LoggMeldingResponse getloggMeldingResponse() {
        return LoggMeldingResponse.builder().id("LOGG123").build();
    }

    private LoggMeldingRequest getLoggMeldingRequest() {
        return LoggMeldingRequest.builder()
                .antallAarLagres(1234)
                .avsender("dokdisteformidling")
                .mottaker("***gammelt_fnr***")
                .joarkRef("429436507")
                .meldingsId(any())
                .build();

    }


}
