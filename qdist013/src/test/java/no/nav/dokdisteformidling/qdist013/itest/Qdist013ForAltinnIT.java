package no.nav.dokdisteformidling.qdist013.itest;

import com.google.cloud.storage.StorageException;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import no.nav.dokdisteformidling.exception.technical.BucketFailedToDownloadTechnicalException;
import no.nav.dokdisteformidling.storage.BucketStorage;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.AVTALTMELDING_PROCESS;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static no.nav.dokdisteformidling.qdist013.TestUtil.classpathToString;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

class Qdist013ForAltinnIT extends AbstractQdist013IntegrationTest {

	private static final String DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK = "dokumentObjektReferanseHoveddok";
	private static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 = "dokumentObjektReferanseVedlegg1";
	private static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 = "dokumentObjektReferanseVedlegg2";
	private static final String HOVEDDOK_TEST_CONTENT = "HOVEDDOK_TEST_CONTENT";
	private static final String VEDLEGG1_TEST_CONTENT = "VEDLEGG1_TEST_CONTENT";
	private static final String VEDLEGG2_TEST_CONTENT = "VEDLEGG2_TEST_CONTENT";
	private static final String PDL_PERSONNAVN_HAPPY = "pdl/personnavn_happy.json";
	private static final String PDL_FORNAVN_NULL = "pdl/personnavn_nullfornavn.json";
	private static final String PDL_IDENT_NOT_FOUND = "pdl/pdlident_not_found.json";
	private static final String PDL_IDENT_HAPPY = "pdl/pdlident_happy.json";

	@Autowired
	private Queue qdist013;

	@Autowired
	private Queue qdist013FunksjonellFeil;
	@Autowired
	private Queue backoutQueue;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private BucketStorage bucketStorage;

	@BeforeEach
	public void setUp() {
		Mockito.reset(bucketStorage);

		stubAzure();
		when(bucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString()))
				.thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(HOVEDDOK_TEST_CONTENT.getBytes()).build()));
		when(bucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1), anyString()))
				.thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(VEDLEGG1_TEST_CONTENT.getBytes()).build()));
		when(bucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2), anyString()))
				.thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(VEDLEGG2_TEST_CONTENT.getBytes()).build()));
	}

	@Test
	void shouldUploadFileToAltinnWhenAktoerIdHappyCase() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubPostMaskinporten();
		stubGetServiceRegistry();
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed();
		stubPostJuridiskLoggLagre();
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifyAltinnUploadWithPostProcessing);
	}

	@Test
	void shouldUploadFileToAltinnWhenFnrHappyCase() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-fnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubPostMaskinporten();
		stubGetServiceRegistry();
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed();
		stubPostJuridiskLoggLagre();
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifyAltinnUploadWithPostProcessing);
	}

	@Test
	void shouldUploadFileToAltinnWhenOrgnrHappyCase() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg();
		stubPostMaskinporten();
		stubGetServiceRegistry();
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed();
		stubPostJuridiskLoggLagre();
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifyAltinnUploadWithPostProcessing);
	}

	@Test
	void shouldUploadFileToAltinnWhenLightweightSafDatoJournalfoertErNull() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-datojournalfoert-null.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubPostMaskinporten();
		stubGetServiceRegistry();
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed();
		stubPostJuridiskLoggLagre();
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifyAltinnUploadWithPostProcessing);
	}

	@Test
	void shouldUploadFileToAltinnWhenLightweightSafSakDatoOpprettetErNull() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-sak-datoopprettet-null.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubPostMaskinporten();
		stubGetServiceRegistry();
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed();
		stubPostJuridiskLoggLagre();
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifyAltinnUploadWithPostProcessing);
	}

	@Test
	void shouldThrowExceptionWhenDatoJournalfoertErNullInJpQdist013() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-relevantdato-null.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-datojournalfoert-null.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldGiDokdistadminFunctionalExceptionWhenNotFoundForHenting() {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(
				aResponse().withStatus(NOT_FOUND.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowBucketFailedToDownloadTechnicalExceptionVedStorageException() {
		when(bucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString())).thenThrow(new StorageException(1, "StorageException"));

		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowBucketFailedToDownloadTechnicalException() {
		when(bucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString())).thenThrow(new BucketFailedToDownloadTechnicalException("Fail"));

		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldPutOnBackoutQueueWhenSafJournalpostIkkeFunnetException() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody("")));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowSafJournalpostQueryUnauthorizedException() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowSafJournalpostQueryTechnicalException() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowSafJournalpostValidationException() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-tomJournalpostId.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldPutMessageOnBackoutWhenSafJournalpostIkkeFunnetExceptionUsingLightweightService() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing("queryJournalpostId\":\"448212366\""))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody("")));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(20, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowSafJournalpostQueryUnauthorizedExceptionLightweight() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing("queryJournalpostId\":\"448212366\""))
				.willReturn(aResponse().withStatus(FORBIDDEN.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldGiPersonIkkeFunnetExceptionForNotFoundFraPdl() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(PDL_IDENT_NOT_FOUND, OK.value());

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldGiDokdistIllegalArgumentExceptionForUgyldigResponsFraPdl() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(PDL_FORNAVN_NULL, OK.value());

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@ParameterizedTest
	@EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "NOT_FOUND"})
	void shouldThrowEregFunctionalExceptionFor4xxFraEreg(HttpStatus httpStatus) {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg(httpStatus);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowEregFunctionalExceptionWhenResponseNull() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get(EREG_URL)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withBody((String) null)));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowEregFunctionalExceptionWhenNavnMangler() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg("ereg/mangler_navn.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowEregFunctionalExceptionWhenSammensattnavnMangler() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg("ereg/mangler_sammensattnavn.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldGiDokdistadminFunctionalExceptionWhenForbiddenForOppdatering() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg();
		stubPostMaskinporten();
		stubGetServiceRegistry();
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed();
		stubPostJuridiskLoggLagre();
		stubPutOppdaterForsendelse(FORBIDDEN);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowInvalidForsendelseStatusException() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-oversendtForsendelseStatus.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowKunneIkkeDeserialisereBucketPayloadFunctionalException() {
		when(bucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2), anyString())).thenReturn("notJsonSerializedString");

		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldGiDokdistadminTechnicalExceptionWhenInternalServerErrorForHenting() {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldGiDokdistadminTechnicalExceptionWhenInternalServerErrorForOppdatering() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg();
		stubPostMaskinporten();
		stubGetServiceRegistry();
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed();
		stubPostJuridiskLoggLagre();
		stubPutOppdaterForsendelse(INTERNAL_SERVER_ERROR);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowLagreJuridiskLoggTechnicalException() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg();
		stubPostMaskinporten();
		stubGetServiceRegistry();
		stubPostIntiateBrokerService();
		stubUploadBrokerServiceStreamed();
		stubFor(post(urlMatching("/juridisklogg.*"))
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowEregTechnicalExceptionVedInternalServerErrorFraEreg() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg(INTERNAL_SERVER_ERROR);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowTechnicalExceptionAndRetryWhenServiceRegistryReturnsError() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg();
		stubPostMaskinporten();
		stubFor(get(urlMatching("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + AVTALTMELDING_PROCESS))
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(20, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowInitiateBrokerServiceTechnicalException() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg();
		stubPostMaskinporten();
		stubGetServiceRegistry();

		stubFor(post(urlMatching("/brokerserviceexternal"))
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@ParameterizedTest
	@ValueSource(strings = {PDL_IDENT_HAPPY, PDL_PERSONNAVN_HAPPY})
	void shouldGiPdlHentPersonTechnicalExceptionForInternalServerErrorFraPdl(String filePath) {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(filePath, INTERNAL_SERVER_ERROR.value());

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowSafJournalpostQueryTechnicalExceptionLightweight() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubGetSecurityToken();
		stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing("queryJournalpostId\":\"448212366\""))
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowStsTechnicalException() {
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid")
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	private void verifyAltinnUploadWithPostProcessing() {
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyPostIntiateBrokerService();
		verifyPostUploadBrokerServiceStreamed();
		verifyPostJuridiskLoggLagre();
		verifyPutAdministrerforsendelse();
	}

	private void verifyPostSafJournalpost() {
		verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safQdist013GraphQlRequest.json"))));
	}

	private void verifyPostSafJournalpostLightweight() {
		verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
	}

	private void verifyPostIntiateBrokerService() {
		verify(1, postRequestedFor(urlMatching("/brokerserviceexternal")));
	}

	private void verifyPostUploadBrokerServiceStreamed() {
		verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternalstreamed/upload")));
	}


	private void verifyPostJuridiskLoggLagre() {
		verify(1, postRequestedFor(urlEqualTo("/juridisklogg"))
				.withRequestBody(equalToJson(classpathToString("__files/juridisklogg/juridiskloggRequest.json"), true, true))
				.withRequestBody(matchingJsonPath("$.meldingsId", containing(CALL_ID)))
				.withRequestBody(matchingJsonPath("$.meldingsInnhold")));
	}

	private void verifyPutAdministrerforsendelse() {
		verify(1, putRequestedFor(urlEqualTo(OPPDATERFORSENDELSE_URL)));
	}

	private void assertMessageOnQueue(Queue queue) {
		String message = receive(queue);
		assertNotNull(message);
		assertEquals(message, classpathToString("qdist013/qdist013-happy.xml"));
	}

	@SuppressWarnings("unchecked")
	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
	}

	private void sendStringMessage(Queue queue, final String message) {
		sendStringMessage(queue, message, CALL_ID);
	}

	private void sendStringMessage(Queue queue, final String message, final String callId) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = session.createTextMessage();
			msg.setText(message);
			if (callId != null) {
				msg.setStringProperty("callId", callId);
			}
			return msg;
		});
	}
}
