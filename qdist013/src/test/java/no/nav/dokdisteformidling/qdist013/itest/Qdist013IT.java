package no.nav.dokdisteformidling.qdist013.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
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
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.dokdisteformidling.constants.RetryConstants.MAX_ATTEMPTS_SHORT;
import static no.nav.dokdisteformidling.storage.S3Configuration.BUCKET_NAME;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static wiremock.com.google.common.base.Strings.isNullOrEmpty;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
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
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import wiremock.com.fasterxml.jackson.databind.JsonNode;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
	private static final String ENHETSNR = "4806";
	private static final String SAKSPARTNAVN_PERSON = "Fornavn Etternavn";
	private static final String SAKSPARTNAVN_ORGANISASJON = "TEST1 COMPANY1 TESTING SERVICES LIMITED";
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

	@BeforeEach
	public void setupBefore() {
		CALL_ID = UUID.randomUUID().toString();

		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();

		reset(amazonS3);
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK)))
				.thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(HOVEDDOK_TEST_CONTENT.getBytes()).build()));
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1)))
				.thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(VEDLEGG1_TEST_CONTENT.getBytes()).build()));
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2)))
				.thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(VEDLEGG2_TEST_CONTENT.getBytes()).build()));
	}

	@Test
	public void shouldProcessForsendelseWithAktoerId() {

		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetTpsHentPersonNavn("***gammelt_fnr***");
		stubGetAktoerregisterHentIdentForAktoerId("***gammelt_fnr***09");
		stubPostIntegrasjonspunktCreateMessage();
		stubPutIntegrasjonspunktLastOppFil();
		stubPostIntegrasjonspunktSendMelding();
		stubPostJuridiskLoggLagre();
		stubPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId();

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			verifyAllStubs("", "***gammelt_fnr***", "***gammelt_fnr***09", 8);
		});

	}

	@Test
	public void shouldProcessForsendelseWithFnr() {

		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-fnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetTpsHentPersonNavn("***gammelt_fnr***");
		stubPostIntegrasjonspunktCreateMessage();
		stubPutIntegrasjonspunktLastOppFil();
		stubPostIntegrasjonspunktSendMelding();
		stubPostJuridiskLoggLagre();
		stubPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId();

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			verifyAllStubs("", "***gammelt_fnr***", "", 7);
		});

	}

	@Test
	public void shouldProcessForsendelseWithOrgnr() {

		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubPostIntegrasjonspunktCreateMessage();
		stubPutIntegrasjonspunktLastOppFil();
		stubPostIntegrasjonspunktSendMelding();
		stubPostJuridiskLoggLagre();
		stubPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId();

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			verifyAllStubs("123456789", "", "", 6);
		});

	}

	@Test
	public void shouldThrowRdist001HentForsendelseFunctionalException() {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
	}

	@Test
	public void shouldThrowRdist001HentForsendelseTechnicalException() {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
	}

	@Test
	public void shouldThrowInvalidForsendelseStatusException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-oversendtForsendelseStatus.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
	}

	@Test
	public void shouldThrowKunneIkkeDeserialisereS3PayloadFunctionalException() {
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME),
				eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2))).thenReturn("notJsonSerializedString");

		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
	}

	@Test
	public void shouldThrowS3FailedToGetDocumentTechnicalExceptionVedSdkClientException() {
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME),
				eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK))).thenThrow(new SdkClientException("SdkClientException"));

		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
	}

	@Test
	public void shouldThrowS3FailedToGetDocumentTechnicalExceptionVedSecurityException() {
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK))).thenThrow(new SecurityException("SecurityException"));

		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
	}

	@Test
	public void shouldThrowStsTechnicalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
	}

	@Test
	public void shouldThrowSafJournalpostIkkeFunnetFunctionalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
				.withBody("")));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(1);
		verify(1, postRequestedFor(urlEqualTo("/safgraphql")));
	}

	@Test
	public void shouldThrowSafJournalpostQueryUnauthorizedException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(1);
		verify(1, postRequestedFor(urlEqualTo("/safgraphql")));
	}

	@Test
	public void shouldThrowSafJournalpostQueryTechnicalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verifyGetForsendelse();
		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(MAX_ATTEMPTS_SHORT, postRequestedFor(urlEqualTo("/safgraphql")));
	}

	@Test
	public void shouldThrowSafJournalpostValidationException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-tomJournalpostId.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(1);
		verify(1, postRequestedFor(urlEqualTo("/safgraphql")));
	}

	@Test
	public void shouldThrowSafJournalpostIkkeFunnetFunctionalExceptionLightweight() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing("queryJournalpostId\":\"448212366\""))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
						.withBody("")));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(2);
		verifyPostSafJournalpost();
		verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
	}

	@Test
	public void shouldThrowSafJournalpostQueryUnauthorizedExceptionLightweight() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing("queryJournalpostId\":\"448212366\""))
				.willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(2);
		verifyPostSafJournalpost();
		verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
	}

	@Test
	public void shouldThrowSafJournalpostQueryTechnicalExceptionLightweight() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing("queryJournalpostId\":\"448212366\""))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(1 + MAX_ATTEMPTS_SHORT);
		verifyPostSafJournalpost();
		verify(MAX_ATTEMPTS_SHORT, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
	}

	@Test
	public void shouldThrowSafJournalpostValidationExceptionLightweightWhenTomJournalfoertAvNavn() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-tomJournalfoertAvNavn.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(2);
		verifyPostSafJournalpost();
		verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
	}

	@Test
	public void shouldThrowSafJournalpostValidationExceptionLightweightWhenUtenDatoJournalfoert() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-utenDatoJournalfoert.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(2);
		verifyPostSafJournalpost();
		verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
	}

	@Test
	public void shouldThrowAktoerHentIdentForAktoerIdFunctionalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
				.withHeader("Nav-Personidenter", equalTo("***gammelt_fnr***09"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
				.willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6 + 1);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
				.withHeader("Nav-Personidenter", equalTo("***gammelt_fnr***09"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
	}

	@Test
	public void shouldThrowAktoerHentIdentForAktoerIdFunctionalExceptionIngenResponse() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		String nullStr = null;
		stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
				.withHeader("Nav-Personidenter", equalTo("***gammelt_fnr***09"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
				.willReturn(aResponse().withStatus(HttpStatus.OK
						.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBody(nullStr)));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6 + 1);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
				.withHeader("Nav-Personidenter", equalTo("***gammelt_fnr***09"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
	}

	@Test
	public void shouldThrowAktoerHentIdentForAktoerIdFunctionalExceptionFeilmelding() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
				.withHeader("Nav-Personidenter", equalTo("***gammelt_fnr***09"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
				.willReturn(aResponse().withStatus(HttpStatus.OK
						.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("aktoerregister/aktoerregisterHentIdentForAktoerFeilmelding.json")));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6 + 1);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
				.withHeader("Nav-Personidenter", equalTo("***gammelt_fnr***09"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
	}

	@Test
	public void shouldThrowAktoerHentIdentForAktoerIdFunctionalExceptionIngenIdent() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
				.withHeader("Nav-Personidenter", equalTo("***gammelt_fnr***09"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
				.willReturn(aResponse().withStatus(HttpStatus.OK
						.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("aktoerregister/aktoerregisterHentIdentForAktoerIngenIdent.json")));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6 + 1);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
				.withHeader("Nav-Personidenter", equalTo("***gammelt_fnr***09"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
	}

	@Test
	public void shouldThrowAktoerHentIdentForAktoerIdTechnicalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
				.withHeader("Nav-Personidenter", equalTo("***gammelt_fnr***09"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6 + MAX_ATTEMPTS_SHORT);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
				.withHeader("Nav-Personidenter", equalTo("***gammelt_fnr***09"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
	}

	@Test
	public void shouldThrowTpsHentNavnFunctionalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-fnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get("/tps/v1/navn")
				.withHeader("Nav-Personident", equalTo("***gammelt_fnr***"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6 + 1);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(1, getRequestedFor(urlEqualTo("/tps/v1/navn"))
				.withHeader("Nav-Personident", equalTo("***gammelt_fnr***"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
	}

	@Test
	public void shouldThrowTpsHentNavnTechnicalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-fnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get("/tps/v1/navn")
				.withHeader("Nav-Personident", equalTo("***gammelt_fnr***"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6 + MAX_ATTEMPTS_SHORT);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/tps/v1/navn"))
				.withHeader("Nav-Personident", equalTo("***gammelt_fnr***"))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
	}

	@Test
	public void shouldThrowEregHentNoekkelinfoFunctionalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo")
				.willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(1, getRequestedFor(urlEqualTo("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo")));
	}

	@Test
	public void shouldThrowEregHentNoekkelinfoFunctionalExceptionIngenResponse() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		String nullStr = null;
		stubFor(get("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBody(nullStr)));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(1, getRequestedFor(urlEqualTo("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo")));
	}

	@Test
	public void shouldThrowEregHentNoekkelinfoFunctionalExceptionManglerNavn() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("ereg/eregHentNavn_manglerNavn.json")));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(1, getRequestedFor(urlEqualTo("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo")));
	}

	@Test
	public void shouldThrowEregHentNoekkelinfoTechnicalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo")));
	}

	@Test
	public void shouldThrowIntegrasjonspunktRequestFunctionalExceptionOpprettMelding() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubFor(post("/integrasjonspunkt/api/messages/out")
				.willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyGetEregHentOrgNavn("123456789");
		verify(1, postRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out")).withRequestBody(equalToJson(
				classpathToString("__files/integrasjonspunkt/createMessageRequest.json"), true, true)));
	}

	@Test
	public void shouldThrowIntegrasjonspunktRequestTechnicalExceptionOpprettMelding() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubFor(post("/integrasjonspunkt/api/messages/out")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyGetEregHentOrgNavn("123456789");
		verify(MAX_ATTEMPTS_SHORT, postRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out")).withRequestBody(equalToJson(
				classpathToString("__files/integrasjonspunkt/createMessageRequest.json"), true, true)));
	}

	@Test
	public void shouldThrowIntegrasjonspunktRequestFunctionalExceptionLastOppFil() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubPostIntegrasjonspunktCreateMessage();
		stubFor(put(urlMatching("/integrasjonspunkt/api/messages/out/.*"))
				.willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyGetEregHentOrgNavn("123456789");
		verifyPostIntegrasjonspunktCreateMessage();
		String bestillingsId = findBestillingsId();
		verify(1, putRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out/" + bestillingsId))
				.withHeader(CONTENT_DISPOSITION, equalTo("attachment; name=Tittel; filename=448212366-463791441-PRODUKSJON-PDF"))
				.withRequestBody(binaryEqualTo(HOVEDDOK_TEST_CONTENT.getBytes())));
	}

	@Test
	public void shouldThrowIntegrasjonspunktRequestTechnicalExceptionLastOppFil() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubPostIntegrasjonspunktCreateMessage();
		stubFor(put(urlMatching("/integrasjonspunkt/api/messages/out/.*"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyGetEregHentOrgNavn("123456789");
		verifyPostIntegrasjonspunktCreateMessage();
		String bestillingsId = findBestillingsId();
		verify(MAX_ATTEMPTS_SHORT, putRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out/" + bestillingsId))
				.withHeader(CONTENT_DISPOSITION, equalTo("attachment; name=Tittel; filename=448212366-463791441-PRODUKSJON-PDF"))
				.withRequestBody(binaryEqualTo(HOVEDDOK_TEST_CONTENT.getBytes())));
	}

	@Test
	public void shouldThrowIntegrasjonspunktRequestFunctionalExceptionSendMelding() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubPostIntegrasjonspunktCreateMessage();
		stubPutIntegrasjonspunktLastOppFil();
		stubFor(post(urlMatching("/integrasjonspunkt/api/messages/out/.*"))
				.willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyGetEregHentOrgNavn("123456789");
		verifyPostIntegrasjonspunktCreateMessage();
		String bestillingsId = findBestillingsId();
		verifyPutIntegrasjonspunktLastOppFilHoveddokument(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg1(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg2(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilArkvimelding(bestillingsId, "123456789", "");
		verify(1, postRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out/" + bestillingsId)).withRequestBody(absent()));
	}

	@Test
	public void shouldThrowIntegrasjonspunktRequestTechnicalExceptionSendMelding() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubPostIntegrasjonspunktCreateMessage();
		stubPutIntegrasjonspunktLastOppFil();
		stubFor(post(urlMatching("/integrasjonspunkt/api/messages/out/.*"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyGetEregHentOrgNavn("123456789");
		verifyPostIntegrasjonspunktCreateMessage();
		String bestillingsId = findBestillingsId();
		verifyPutIntegrasjonspunktLastOppFilHoveddokument(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg1(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg2(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilArkvimelding(bestillingsId, "123456789", "");
		verify(MAX_ATTEMPTS_SHORT, postRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out/" + bestillingsId)).withRequestBody(absent()));
	}

	@Test
	public void shouldThrowLagreJuridiskLoggFunctionalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubPostIntegrasjonspunktCreateMessage();
		stubPutIntegrasjonspunktLastOppFil();
		stubPostIntegrasjonspunktSendMelding();
		stubFor(post(urlMatching("/juridisklogg.*"))
				.willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyGetEregHentOrgNavn("123456789");
		verifyPostIntegrasjonspunktCreateMessage();
		String bestillingsId = findBestillingsId();
		verifyPutIntegrasjonspunktLastOppFilHoveddokument(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg1(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg2(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilArkvimelding(bestillingsId, "123456789", "");
		verifyPostIntegrasjonspunktSendMelding(bestillingsId);
		verify(1, postRequestedFor(urlEqualTo("/juridisklogg"))
				.withRequestBody(equalToJson(classpathToString("__files/juridisklogg/juridiskloggRequest.json"), true, true)));
	}

	@Test
	public void shouldThrowLagreJuridiskLoggTechnicalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubPostIntegrasjonspunktCreateMessage();
		stubPutIntegrasjonspunktLastOppFil();
		stubPostIntegrasjonspunktSendMelding();
		stubFor(post(urlMatching("/juridisklogg.*"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyGetEregHentOrgNavn("123456789");
		verifyPostIntegrasjonspunktCreateMessage();
		String bestillingsId = findBestillingsId();
		verifyPutIntegrasjonspunktLastOppFilHoveddokument(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg1(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg2(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilArkvimelding(bestillingsId, "123456789", "");
		verifyPostIntegrasjonspunktSendMelding(bestillingsId);
		verify(MAX_ATTEMPTS_SHORT, postRequestedFor(urlEqualTo("/juridisklogg"))
				.withRequestBody(equalToJson(classpathToString("__files/juridisklogg/juridiskloggRequest.json"), true, true)));
	}

	@Test
	public void shouldThrowRdist001OppdaterForsendelseFunctionalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubPostIntegrasjonspunktCreateMessage();
		stubPutIntegrasjonspunktLastOppFil();
		stubPostIntegrasjonspunktSendMelding();
		stubPostJuridiskLoggLagre();
		stubFor(put(urlMatching("/administrerforsendelse\\?forsendelseId=" + FORSENDELSE_ID + "\\&forsendelseStatus=OVERSENDT\\&konversasjonsId=.*"))
				.willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(qdist013FunksjonellFeil);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyGetEregHentOrgNavn("123456789");
		verifyPostIntegrasjonspunktCreateMessage();
		String bestillingsId = findBestillingsId();
		verifyPutIntegrasjonspunktLastOppFilHoveddokument(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg1(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg2(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilArkvimelding(bestillingsId, "123456789", "");
		verifyPostIntegrasjonspunktSendMelding(bestillingsId);
		verifyPostJuridiskLoggLagre();
		String conversationId = findConversationId();
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID +
				"&forsendelseStatus=OVERSENDT&konversasjonsId=" + conversationId)));
	}

	@Test
	public void shouldThrowRdist001OppdaterForsendelseTechnicalException() {
		stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubGetEregHentOrgNavn("123456789");
		stubPostIntegrasjonspunktCreateMessage();
		stubPutIntegrasjonspunktLastOppFil();
		stubPostIntegrasjonspunktSendMelding();
		stubPostJuridiskLoggLagre();
		stubFor(put(urlMatching("/administrerforsendelse\\?forsendelseId=" + FORSENDELSE_ID + "\\&forsendelseStatus=OVERSENDT\\&konversasjonsId=.*"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertMessageOnQueue(backoutQueue);
		});

		verifyGetForsendelse();
		verifyGetSecurityToken(6);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyGetEregHentOrgNavn("123456789");
		verifyPostIntegrasjonspunktCreateMessage();
		String bestillingsId = findBestillingsId();
		verifyPutIntegrasjonspunktLastOppFilHoveddokument(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg1(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg2(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilArkvimelding(bestillingsId, "123456789", "");
		verifyPostIntegrasjonspunktSendMelding(bestillingsId);
		verifyPostJuridiskLoggLagre();
		String conversationId = findConversationId();
		verify(MAX_ATTEMPTS_SHORT, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID +
				"&forsendelseStatus=OVERSENDT&konversasjonsId=" + conversationId)));
	}

	private void stubPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId() {
		stubFor(put(urlMatching("/administrerforsendelse\\?forsendelseId=" + FORSENDELSE_ID + "\\&forsendelseStatus=OVERSENDT\\&konversasjonsId=.*"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void verifyPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId(String conversationId) {
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID +
				"&forsendelseStatus=OVERSENDT&konversasjonsId=" + conversationId)));
	}

	private void stubPostJuridiskLoggLagre() {
		stubFor(post(urlMatching("/juridisklogg.*"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void verifyPostJuridiskLoggLagre() {
		verify(1, postRequestedFor(urlEqualTo("/juridisklogg"))
				.withRequestBody(equalToJson(classpathToString("__files/juridisklogg/juridiskloggRequest.json"), true, true))
				.withRequestBody(matchingJsonPath("$.meldingsId", containing(CALL_ID)))
				.withRequestBody(matchingJsonPath("$.meldingsInnhold")));
	}

	private void stubPostIntegrasjonspunktSendMelding() {
		stubFor(post(urlMatching("/integrasjonspunkt/api/messages/out/.*"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void verifyPostIntegrasjonspunktSendMelding(String conversationId) {
		verify(1, postRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out/" + conversationId)).withRequestBody(absent()));
	}

	private void stubPutIntegrasjonspunktLastOppFil() {
		stubFor(put(urlMatching("/integrasjonspunkt/api/messages/out/.*"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void verifyPutIntegrasjonspunktLastOppFilHoveddokument(String conversationId) {
		verify(1, putRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out/" + conversationId))
				.withHeader(CONTENT_DISPOSITION, equalTo("attachment; name=Tittel; filename=448212366-463791441-PRODUKSJON-PDF"))
				.withRequestBody(binaryEqualTo(HOVEDDOK_TEST_CONTENT.getBytes())));
	}

	private void verifyPutIntegrasjonspunktLastOppFilVedlegg1(String conversationId) {
		verify(1, putRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out/" + conversationId))
				.withHeader(CONTENT_DISPOSITION, equalTo("attachment; name=Tittel Vedlegg 1; filename=448212366-463791442-SLADDET-PDF"))
				.withRequestBody(binaryEqualTo(VEDLEGG1_TEST_CONTENT.getBytes())));
	}

	private void verifyPutIntegrasjonspunktLastOppFilVedlegg2(String conversationId) {
		verify(1, putRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out/" + conversationId))
				.withHeader(CONTENT_DISPOSITION, equalTo("attachment; name=Tittel Vedlegg 2, Fra FORNAVN ETTERNAVN; filename=448212366-463791443-ARKIV-PNG"))
				.withRequestBody(binaryEqualTo(VEDLEGG2_TEST_CONTENT.getBytes())));
	}

	private void verifyPutIntegrasjonspunktLastOppFilArkvimelding(String conversationId, String orgnr, String fnr) {
		verify(1, putRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out/" + conversationId))
				.withHeader(CONTENT_DISPOSITION, equalTo("attachment; name=arkivmelding; filename=arkivmelding.xml"))
				.withRequestBody(equalToXml(classpathToString("__files/integrasjonspunkt/arkivMeldingRequest.xml"), true)));

		// separat verifisering av spesifikke elementer i arkivmelding, da det gir tydeligere feedback ved evt. feil
		verifySpecificArkivMeldingElements(conversationId, orgnr, fnr);
	}

	private void verifySpecificArkivMeldingElements(String conversationId, String orgnr, String fnr) {
		String sakspartID = orgnr;
		if (!isNullOrEmpty(fnr)) {
			sakspartID = fnr;
		}
		String sakspartNavn = SAKSPARTNAVN_PERSON;
		if (!isNullOrEmpty(orgnr)) {
			sakspartNavn = SAKSPARTNAVN_ORGANISASJON;
		}
		verify(1, putRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out/" + conversationId))
				.withHeader(CONTENT_DISPOSITION, equalTo("attachment; name=arkivmelding; filename=arkivmelding.xml"))
				.withRequestBody(containing("<meldingId>" + CALL_ID + "</meldingId>"))
				.withRequestBody(containing("<sakspartID>" + sakspartID + "</sakspartID>"))
				.withRequestBody(containing("<sakspartNavn>" + sakspartNavn + "</sakspartNavn>")));
	}

	private void stubPostIntegrasjonspunktCreateMessage() {
		stubFor(post("/integrasjonspunkt/api/messages/out")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void verifyPostIntegrasjonspunktCreateMessage() {
		verify(1, postRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out"))
				.withRequestBody(equalToJson(classpathToString("__files/integrasjonspunkt/createMessageRequest.json"), true, true))
				.withRequestBody(matchingJsonPath("$..documentIdentification.instanceIdentifier", containing(CALL_ID)))
				.withRequestBody(matchingJsonPath("$..documentIdentification.creationDateAndTime"))
				.withRequestBody(matchingJsonPath("$..businessScope.scope[0].scopeInformation[0].expectedResponseDateTime")));
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

	private void verifyGetAktoerregisterHentIdentForAktoerId(String aktoerId) {
		verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
				.withHeader("Nav-Personidenter", equalTo(aktoerId))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
	}

	private void stubGetTpsHentPersonNavn(String fnr) {
		stubFor(get("/tps/v1/navn")
				.withHeader("Nav-Personident", equalTo(fnr))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("tps/tpsHentNavn_happy.json")));
	}

	private void verifyGetTpsHentPersonNavn(String fnr) {
		verify(1, getRequestedFor(urlEqualTo("/tps/v1/navn"))
				.withHeader("Nav-Personident", equalTo(fnr))
				.withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
	}

	private void stubGetEregHentOrgNavn(String orgnr) {
		stubFor(get("/ereg/v1/organisasjon/" + orgnr + "/noekkelinfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("ereg/eregHentNavn_happy.json")));
	}

	private void verifyGetEregHentOrgNavn(String orgnr) {
		verify(1, getRequestedFor(urlEqualTo("/ereg/v1/organisasjon/" + orgnr + "/noekkelinfo")));
	}

	private void stubGetSecurityToken() {
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse_happy.json")));
	}

	private void verifyGetSecurityToken(int stsCount) {
		verify(stsCount, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
	}

	private void stubPostSafJournalpost(String stringInRequestBody, String returnBodyFileName) {
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing(stringInRequestBody))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
						.withBodyFile(returnBodyFileName)));
	}

	private void verifyPostSafJournalpost() {
		verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safQdist013GraphQlRequest.json"))));
	}

	private void verifyPostSafJournalpostLightweight() {
		// no caching
		verify(5, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
	}

	private void stubGetForsendelse(String bodyClasspath) {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString(bodyClasspath).replace("insertCallIdHere", CALL_ID))));
	}

	private void verifyGetForsendelse() {
		verify(1, getRequestedFor(urlMatching("/administrerforsendelse/" + FORSENDELSE_ID)));
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

	private void verifyAllStubs(String orgnr, String fnr, String aktoerId, int stsCount) {
		verifyGetForsendelse();
		verifyGetSecurityToken(stsCount);
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();

		if (!isNullOrEmpty(orgnr)) {
			verifyGetEregHentOrgNavn(orgnr);
		} else {
			verifyGetTpsHentPersonNavn(fnr);
		}
		if (!isNullOrEmpty(aktoerId)) {
			verifyGetAktoerregisterHentIdentForAktoerId(aktoerId);
		}

		verifyPostIntegrasjonspunktCreateMessage();
		String bestillingsId = findBestillingsId();
		verifyPutIntegrasjonspunktLastOppFilHoveddokument(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg1(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilVedlegg2(bestillingsId);
		verifyPutIntegrasjonspunktLastOppFilArkvimelding(bestillingsId, orgnr, fnr);
		verifyPostIntegrasjonspunktSendMelding(bestillingsId);

		verifyPostJuridiskLoggLagre();
		String conversationId = findConversationId();
		verifyPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId(conversationId);
	}

	private void assertMessageOnQueue(Queue queue) {
		String message = receive(queue);
		assertNotNull(message);
		assertEquals(message, classpathToString("qdist013/qdist013-happy.xml"));
	}

	private String findBestillingsId() {
		List<LoggedRequest> loggedRequests = findAll(postRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out")));
		String requestStr = loggedRequests.get(0).getBodyAsString();
		try {
			JsonNode requestTree = new ObjectMapper().readTree(requestStr);
			return requestTree.get("standardBusinessDocumentHeader").get("documentIdentification")
					.get("instanceIdentifier").asText();
		} catch (Exception e) {
			fail("Fant ikke konversasjonsId. Feil: " + e.getMessage(), e.getCause());
			return null;
		}
	}

	private String findConversationId() {
		List<LoggedRequest> loggedRequests = findAll(postRequestedFor(urlEqualTo("/integrasjonspunkt/api/messages/out")));
		String requestStr = loggedRequests.get(0).getBodyAsString();
		try {
			JsonNode requestTree = new ObjectMapper().readTree(requestStr);
			return requestTree.get("standardBusinessDocumentHeader").get("businessScope")
					.get("scope").get(0).get("instanceIdentifier").asText();
		} catch (Exception e) {
			fail("Fant ikke konversasjonsId. Feil: " + e.getMessage(), e.getCause());
			return null;
		}
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
