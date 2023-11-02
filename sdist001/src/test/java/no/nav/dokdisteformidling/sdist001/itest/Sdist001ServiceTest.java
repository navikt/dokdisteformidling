package no.nav.dokdisteformidling.sdist001.itest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.eformidling.AltinnEformidling;
import no.nav.dokdisteformidling.consumer.eformidling.Eformidling;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadResponse;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.trygderetten.json.KvitteringStatus;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLoggConsumer;
import no.nav.dokdisteformidling.consumer.juridisklogg.LagreJuridiskLoggMapper;
import no.nav.dokdisteformidling.consumer.juridisklogg.LoggMeldingRequest;
import no.nav.dokdisteformidling.consumer.juridisklogg.LoggMeldingResponse;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdisteformidling.consumer.rdist001.HentEformidlingforsendelserResponseTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentEformidlingforsendelserResponseTo.ForsendelseTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponse;
import no.nav.dokdisteformidling.sdist001.Sdist001Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.dokdisteformidling.sdist001.domain.to.AltinnKvitteringStatus.LEST;
import static no.nav.dokdisteformidling.sdist001.domain.to.AltinnKvitteringStatus.LEVERT;
import static no.nav.dokdisteformidling.sdist001.domain.to.AltinnKvitteringStatus.LEVETID_UTLOPT;
import static no.nav.dokdisteformidling.sdist001.domain.to.AltinnKvitteringStatus.MOTTATT;
import static no.nav.dokdisteformidling.sdist001.domain.to.AltinnKvitteringStatus.SENDT;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.BEKREFTET;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.EKSPEDERT;
import static no.nav.dokdisteformidling.sdist001.domain.to.ForsendelseStatus.OVERSENDT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
class Sdist001ServiceTest {

	private final static String SENDERS_REFERENCE_1 = "33259df3-18ae-45e6-9861-47f42e119a11";
	private final static String SENDERS_REFERENCE_2 = "33259df3-18ae-45e6-9861-47f42e119a12";
	private final static String SENDERS_REFERENCE_3 = "33259df3-18ae-45e6-9861-47f42e119a13";
	private final static String SENDERS_REFERENCE_4 = "33259df3-18ae-45e6-9861-47f42e119a14";

	private final static String CONVERSATION_ID_1 = "f1b3002b-1dea-4c14-8072-8c191183d04a";
	private final static String CONVERSATION_ID_2 = "f1b3002b-1dea-4c14-8072-8c191183d04b";
	private final static String CONVERSATION_ID_3 = "f1b3002b-1dea-4c14-8072-8c191183d04c";
	private final static String CONVERSATION_ID_4 = "f1b3002b-1dea-4c14-8072-8c191183d04d";

	private static final String FORSENDELSE_ID_1 = "1231";
	private static final String FORSENDELSE_ID_2 = "1232";
	private static final String FORSENDELSE_ID_3 = "1233";
	private static final String FORSENDELSE_ID_4 = "1234";

	private Eformidling eformidling;
	private Sdist001Service sdist001Service;
	private AdministrerForsendelse administrerForsendelse;
	private JuridiskLogg juridiskLogg;
	private final LagreJuridiskLoggMapper lagreJuridiskLoggMapper = new LagreJuridiskLoggMapper();

	@BeforeEach
	public void setUp() {
		eformidling = mock(AltinnEformidling.class);
		administrerForsendelse = mock(AdministrerForsendelseConsumer.class);
		juridiskLogg = mock(JuridiskLoggConsumer.class);
		sdist001Service = new Sdist001Service(administrerForsendelse, juridiskLogg, eformidling, lagreJuridiskLoggMapper);
	}

	@Test
	void navForsendelserShouldLoggWhenKvitteringStatusFraTrygdErMottatt() throws IOException {
		InputStream inputStream = new ClassPathResource("__files/rdist001/getForsendelse_forAltinnTest.json").getInputStream();

		HentForsendelseResponse forsendelseResponseTo = deserializeToObject(inputStream, HentForsendelseResponse.class);

		when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(hentEformidlingforsendelserResponseTo());
		when(administrerForsendelse.hentForsendelse(anyLong())).thenReturn(forsendelseResponseTo);
		when(eformidling.hent()).thenReturn(getDownloadResponse());
		when(juridiskLogg.lagreJuridiskLogg(getLoggMeldingRequest())).thenReturn(getloggMeldingResponse());

		sdist001Service.oppdaterDokDistEformidlingStatus();

		verify(administrerForsendelse, times(1)).hentEformidlingForsendelser();
		verify(administrerForsendelse, times(1)).hentForsendelse(anyLong());
		verify(eformidling, times(1)).hent();
		verify(juridiskLogg, times(1)).lagreJuridiskLogg(getLoggMeldingRequest());
	}

	@Test
	void navForsendelserShouldSetToExpedertWhenKvitteringStatusFraTrygdErLevert() throws IOException {
		InputStream inputStream = new ClassPathResource("__files/rdist001/getForsendelse_forAltinnTest.json").getInputStream();

		HentForsendelseResponse forsendelseResponseTo = deserializeToObject(inputStream, HentForsendelseResponse.class);

		when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(hentEformidlingforsendelserResponseTo());
		when(administrerForsendelse.hentForsendelse(anyLong())).thenReturn(forsendelseResponseTo);
		when(eformidling.hent()).thenReturn(getDownloadResponseLevert());
		when(juridiskLogg.lagreJuridiskLogg(getLoggMeldingRequest())).thenReturn(getloggMeldingResponse());

		sdist001Service.oppdaterDokDistEformidlingStatus();

		verify(administrerForsendelse, times(1)).hentEformidlingForsendelser();
		verify(administrerForsendelse, times(2)).hentForsendelse(anyLong());
		verify(eformidling, times(1)).hent();
		verify(juridiskLogg, times(2)).lagreJuridiskLogg(getLoggMeldingRequest());
	}

	public <T> T deserializeToObject(InputStream inputStream, Class<T> tClass) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(inputStream, tClass);
	}

	private List<DownloadResponse> getDownloadResponse() {
		return asList(DownloadResponse.builder()
						.conversationId(CONVERSATION_ID_1)
						.kvitteringStatus(KvitteringStatus.builder()
								.status(MOTTATT.name())
								.build())
						.sendersReference(SENDERS_REFERENCE_1)
						.sendtDate(LocalDateTime.now().toString())
						.build(),
				DownloadResponse.builder()
						.conversationId(CONVERSATION_ID_2)
						.kvitteringStatus(KvitteringStatus.builder()
								.status(LEVERT.name())
								.build())
						.sendersReference(SENDERS_REFERENCE_2)
						.sendtDate(LocalDateTime.now().toString())
						.build(),
				DownloadResponse.builder()
						.conversationId(CONVERSATION_ID_3)
						.kvitteringStatus(KvitteringStatus.builder()
								.status(SENDT.name())
								.build())
						.sendersReference(SENDERS_REFERENCE_3)
						.sendtDate(LocalDateTime.now().toString())
						.build(),
				DownloadResponse.builder()
						.conversationId(CONVERSATION_ID_4)
						.kvitteringStatus(KvitteringStatus.builder()
								.status(LEVETID_UTLOPT.name())
								.build())
						.sendersReference(SENDERS_REFERENCE_4)
						.sendtDate(LocalDateTime.now().toString())
						.build()


		);
	}

	private List<DownloadResponse> getDownloadResponseLevert() {
		return asList(DownloadResponse.builder()
						.conversationId(CONVERSATION_ID_1)
						.kvitteringStatus(KvitteringStatus.builder()
								.status(LEST.name())
								.build())
						.sendersReference(SENDERS_REFERENCE_1)
						.sendtDate(LocalDateTime.now().toString())
						.build(),
				DownloadResponse.builder()
						.conversationId(CONVERSATION_ID_2)
						.kvitteringStatus(KvitteringStatus.builder()
								.status(LEVERT.name())
								.build())
						.sendersReference(SENDERS_REFERENCE_2)
						.sendtDate(LocalDateTime.now().toString())
						.build(),
				DownloadResponse.builder()
						.conversationId(CONVERSATION_ID_3)
						.kvitteringStatus(KvitteringStatus.builder()
								.status(SENDT.name())
								.build())
						.sendersReference(SENDERS_REFERENCE_3)
						.sendtDate(LocalDateTime.now().toString())
						.build(),
				DownloadResponse.builder()
						.conversationId(CONVERSATION_ID_4)
						.kvitteringStatus(KvitteringStatus.builder()
								.status(LEVETID_UTLOPT.name())
								.build())
						.sendersReference(SENDERS_REFERENCE_4)
						.sendtDate(LocalDateTime.now().toString())
						.build()


		);
	}

	private HentEformidlingforsendelserResponseTo hentEformidlingforsendelserResponseTo() {

		return HentEformidlingforsendelserResponseTo.builder()
				.forsendelser(asList(
						ForsendelseTo.builder()
								.distribusjonKanal("TRYGDERETTEN")
								.forsendelseStatus(OVERSENDT.name())
								.forsendelseId(FORSENDELSE_ID_1)
								.konversasjonId(CONVERSATION_ID_1)
								.build(),
						ForsendelseTo.builder()
								.distribusjonKanal("TRYGDERETTEN")
								.forsendelseStatus(BEKREFTET.name())
								.forsendelseId(FORSENDELSE_ID_2)
								.konversasjonId(CONVERSATION_ID_2)
								.build(),
						ForsendelseTo.builder()
								.distribusjonKanal("TRYGDERETTEN")
								.forsendelseStatus(OVERSENDT.name())
								.forsendelseId(FORSENDELSE_ID_3)
								.konversasjonId(CONVERSATION_ID_3)
								.build(),
						ForsendelseTo.builder()
								.distribusjonKanal("TRYGDERETTEN")
								.forsendelseStatus(EKSPEDERT.name())
								.forsendelseId(FORSENDELSE_ID_4)
								.konversasjonId(CONVERSATION_ID_4)
								.build()))
				.build();

	}

	private LoggMeldingResponse getloggMeldingResponse() {
		return LoggMeldingResponse.builder().id("dfafdsa").build();
	}

	private LoggMeldingRequest getLoggMeldingRequest() {
		return LoggMeldingRequest.builder()
				.antallAarLagres(1234)
				.avsender("dokdisteformidling")
				.mottaker("01054049486")
				.joarkRef("429436507")
				.meldingsId(any())
				.build();

	}
}
