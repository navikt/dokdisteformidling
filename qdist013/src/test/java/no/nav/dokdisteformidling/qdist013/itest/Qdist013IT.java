package no.nav.dokdisteformidling.qdist013.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.TKAT020_CACHE;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.TKAT021_CACHE;
import static no.nav.dokdisteformidling.storage.S3Configuration.BUCKET_NAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import com.amazonaws.services.s3.AmazonS3;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdisteformidling.qdist013.itest.config.ApplicationTestConfig;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author Erik Bråten, Visma Consulting
 */
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class Qdist013IT {

	private static final String FORSENDELSE_ID = "33333";
	private static final String DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK = "dokumentObjektReferanseHoveddok";
	private static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 = "dokumentObjektReferanseVedlegg1";
	private static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 = "dokumentObjektReferanseVedlegg2";
	private static final String HOVEDDOK_TEST_CONTENT = "HOVEDDOK_TEST_CONTENT";
	private static final String VEDLEGG1_TEST_CONTENT = "VEDLEGG1_TEST_CONTENT";
	private static final String VEDLEGG2_TEST_CONTENT = "VEDLEGG2_TEST_CONTENT";
	private static final String BEARER_OIDC_TOKEN = "Bearer eyJraWQiOiIyZDYwNjZmNi1mM2ViLTRlYzktYjRlZS0wMzM1Nzg0MDY3MTMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZqb2Fya2FkbWluIiwiYXVkIjpbInNydmpvYXJrYWRtaW4iLCJwcmVwcm9kLmxvY2FsIl0sInZlciI6IjEuMCIsIm5iZiI6MTU1NjI2Nzg0NiwiYXpwIjoic3J2am9hcmthZG1pbiIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE1NTYyNjc4NDYsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTU1NjI3MTQ0NiwiaWF0IjoxNTU2MjY3ODQ2LCJqdGkiOiI5NzVmMjY4YS00ZmI3LTQ2NWMtOTIyZS0xY2Q4OTNjZDEwY2QifQ.e7e1cKmLt0wYSBdURju0pZnplheXl-T5Df7t2QKcOWpKfERKgfSnMOHPYuS80GJbwvfZXE7F_WiTyB2Klsv_shS2Iy_DqqS2qRPUit4fCDyXX4TMBVWWqBY60Wg46NuZGz4kje6z0BcT84cyrQSPKNuVEmy9xcdIXrQ2xzJy9NyOseSvEkUPX4Xj4yfCh6CoEIOsNDQ-hW6XUkbAKjF3nkM6AwSQ2cZTi9T7j12LNw4RQyBwl9PINP8d3t2jeOJ8Gq7xVkzlyL60SHH2UnblBag0UhCYLYIzuSr1lkpvZ_8q5vqg9DXk7CQZGmZNfoOOQsy1pBTyzU3JjhGmBNWZEg";
	private static String CALL_ID;

	@Inject
	private JmsTemplate jmsTemplate;

	@Inject
	private Queue qdist013;

	@Inject
	private Queue qdist013FunksjonellFeil;

	@Inject
	private Queue backoutQueue;

	@Inject
	private AmazonS3 amazonS3;

	@Inject
	private CacheManager cacheManager;


	@BeforeEach
	public void setupBefore() {
		CALL_ID = UUID.randomUUID().toString();

		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();

		cacheManager.getCache(TKAT020_CACHE).clear();
		cacheManager.getCache(TKAT021_CACHE).clear();
		reset(amazonS3);
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK)))
				.thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(HOVEDDOK_TEST_CONTENT.getBytes()).build()));
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1)))
				.thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(VEDLEGG1_TEST_CONTENT.getBytes()).build()));
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2)))
				.thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(VEDLEGG2_TEST_CONTENT.getBytes()).build()));
	}

	@Test
	public void shouldProcessForsendelse() throws Exception {

		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-happy.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetNorg2HentOrgnr("4806");
		stubGetEregHentOrgNavn("");
		stubGetTpsHentPersonNavn("***gammelt_fnr***");
		stubGetAktoerregisterHentIdentForAktoerId("***gammelt_fnr***09");
		stubPostIntegrasjonspunktCreateMessage();
		stubPutIntegrasjonspunktLastOppFil();
		stubPostIntegrasjonspunktSendMelding();
		stubPostJuridiskLoggLagre();
		stubPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId();

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		verifyAllStubs("4806", "", "***gammelt_fnr***", "***gammelt_fnr***09");
	}

	private void stubPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId() {
		stubFor(put(urlMatching("/administrerforsendelse\\?forsendelseId=" + FORSENDELSE_ID + "\\&forsendelseStatus=OVERSENDT\\&konversasjonsId=.*"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void stubPostJuridiskLoggLagre() {
		stubFor(post(urlMatching("/juridisklogg.*"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void stubPostIntegrasjonspunktSendMelding() {
		stubFor(post(urlMatching("/integrasjonspunkt/.*"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void stubPutIntegrasjonspunktLastOppFil() {
		stubFor(put(urlMatching("/integrasjonspunkt/.*"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void stubPostIntegrasjonspunktCreateMessage() {
		stubFor(post("/integrasjonspunkt")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void stubGetAktoerregisterHentIdentForAktoerId(String aktoerId) {
		stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
				.withHeader("Nav-Personidenter", equalTo(aktoerId))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
				.willReturn(aResponse().withStatus(HttpStatus.OK
						.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("aktoerregister/aktoerregisterHentIdentForAktoerHappy.json")));
	}

	private void stubGetTpsHentPersonNavn(String fnr) {
		stubFor(get("/tps/v1/navn")
				.withHeader("Nav-Personident", equalTo(fnr))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("tps/tpsHentNavn_happy.json")));
	}

	private void stubGetEregHentOrgNavn(String orgnr) {
		stubFor(get("/v1/organisasjon/" + orgnr + "/noekkelinfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("ereg/eregHentNavn_happy.json")));
	}

	private void stubGetNorg2HentOrgnr(String enhetsNr) {
		stubFor(get("/norg2/enhet/" + enhetsNr).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("norg2/norg2HentOrgnr_happy.json")));
	}

	private void stubGetSecurityToken() {
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse_happy.json")));
	}

	private void stubPostSafJournalpost(String stringInRequestBody, String returnBodyFileName) {
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing(stringInRequestBody))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
						.withBodyFile(returnBodyFileName)));
	}

	private void stubGetForsendelse(String bodyClasspath) throws IOException {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString(bodyClasspath).replace("insertCallIdHere", CALL_ID))));
	}

	private void sendStringMessage(Queue queue, final String message) {
		sendStringMessage(queue, message, CALL_ID);
	}

	private void sendStringMessage(Queue queue, final String message, final String callId) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = new ActiveMQTextMessage();
			msg.setText(message);
			if (callId != null) {
				msg.setStringProperty("callId", callId);
			}
			return msg;
		});
	}

	@SuppressWarnings("unchecked")
	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
	}

	private void verifyAllStubs(String enhetsNr, String orgnr, String fnr, String aktoerId) {
		//TODO Fill in request files!
		verify(1, getRequestedFor(urlMatching("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(3, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(1, getRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalTo(classpathToString("__files/saf/safQdist013GraphQlResquest.json"))));
		verify(1, getRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalTo(classpathToString("__files/saf/safLightweightGraphQlResponse-happy.json"))));
		verify(1, getRequestedFor(urlEqualTo("/norg2/enhet/" + enhetsNr)));
		verify(1, getRequestedFor(urlEqualTo("/v1/organisasjon/" + orgnr + "/noekkelinfo")));
		verify(1, getRequestedFor(urlEqualTo("/tps/v1/navn"))
				.withHeader("Nav-Personident", equalTo(fnr))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
		verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
				.withHeader("Nav-Personidenter", equalTo(aktoerId))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
		verify(1, postRequestedFor(urlEqualTo("/integrasjonspunkt"))
				.withRequestBody(equalTo(classpathToString("__files/integrasjonspunkt/createMessageRequest.json"))));
		verify(1, putRequestedFor(urlEqualTo("/integrasjonspunkt")) //TODO: assert x3
				.withRequestBody(equalTo(classpathToString("__files/integrasjonspunkt/createMessageRequest.json")))
				.withHeader(CONTENT_DISPOSITION, equalTo("attachment; name=%s; filename=%s")));
		verify(1, postRequestedFor(urlEqualTo("/integrasjonspunkt")));
		verify(1, postRequestedFor(urlEqualTo("/juridisklogg")));
		verify(1, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}


	public static String classpathToString(String classpathResource) {
		try {
			InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
			String message = IOUtils.toString(inputStream, UTF_8);
			IOUtils.closeQuietly(inputStream);
			return message;
		} catch (IOException e) {
			throw new RuntimeException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
		}
	}

}



