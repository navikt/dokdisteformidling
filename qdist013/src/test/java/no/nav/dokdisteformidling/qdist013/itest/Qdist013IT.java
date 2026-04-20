package no.nav.dokdisteformidling.qdist013.itest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.dokdisteformidling.qdist013.TestUtil.classpathToString;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class Qdist013IT extends AbstractIT {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void shouldLagreAvtalemeldingOgVideresendeTilQdist015VedAktoerIdHappyCase() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifyLagreMetadataOgVideresendTilQdist015);
	}

	@Test
	void shouldLagreAvtalemeldingOgVideresendeTilQdist015VedFnrHappyCase() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-fnr.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifyLagreMetadataOgVideresendTilQdist015);
	}

	@Test
	void shouldLagreAvtalemeldingOgVideresendeTilQdist015VedOrgnrHappyCase() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg();
		stubPutOppdaterForsendelse(OK);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(this::verifyLagreMetadataOgVideresendTilQdist015);
	}

	@Test
	void shouldThrowExceptionWhenDatoJournalfoertErNullInJpQdist013() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-relevantdato-null.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-datojournalfoert-null.json");

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
	void shouldPutOnBackoutQueueWhenSafJournalpostIkkeFunnetException() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
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
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowSafJournalpostQueryTechnicalException() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowSafJournalpostValidationException() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-tomJournalpostId.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldPutMessageOnBackoutWhenSafJournalpostIkkeFunnetExceptionUsingLightweightService() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-aktoerId.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing("448212366"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody("")));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(20, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowSafJournalpostQueryUnauthorizedExceptionLightweight() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing("448212366"))
				.willReturn(aResponse().withStatus(FORBIDDEN.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldGiPersonIkkeFunnetExceptionForNotFoundFraPdl() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(PDL_IDENT_NOT_FOUND, OK.value());

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldGiDokdistIllegalArgumentExceptionForUgyldigResponsFraPdl() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(PDL_FORNAVN_NULL, OK.value());

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@ParameterizedTest
	@EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "NOT_FOUND"})
	void shouldThrowEregFunctionalExceptionFor4xxFraEreg(HttpStatus httpStatus) {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg(httpStatus);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowEregFunctionalExceptionWhenResponseNull() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		stubFor(get(EREG_URL)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withBody((String) null)));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowEregFunctionalExceptionWhenNavnMangler() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg("ereg/mangler_navn.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldThrowEregFunctionalExceptionWhenSammensattnavnMangler() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg("ereg/mangler_sammensattnavn.json");

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist013FunksjonellFeil));
	}

	@Test
	void shouldGiDokdistadminFunctionalExceptionWhenForbiddenForOppdatering() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg();
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
	void shouldGiDokdistadminTechnicalExceptionWhenInternalServerErrorForHenting() {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldGiDokdistadminTechnicalExceptionWhenInternalServerErrorForOppdatering() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg();
		stubPutOppdaterForsendelse(INTERNAL_SERVER_ERROR);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowEregTechnicalExceptionVedInternalServerErrorFraEreg() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-orgnr.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		stubEreg(INTERNAL_SERVER_ERROR);

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@ParameterizedTest
	@ValueSource(strings = {PDL_IDENT_HAPPY, PDL_PERSONNAVN_HAPPY})
	void shouldGiPdlHentPersonTechnicalExceptionForInternalServerErrorFraPdl(String filePath) {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-aktoerId.json");
		stubPostSafJournalpost("448212366", "saf/safLightweightGraphQlResponse-happy.json");
		postPdlGraphql(filePath, INTERNAL_SERVER_ERROR.value());

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	@Test
	void shouldThrowSafJournalpostQueryTechnicalExceptionLightweight() {
		stubAzure();
		stubGetForsendelse("__files/dokdistadmin/getForsendelse-happy.json");
		stubPostSafJournalpost("123", "saf/safQdist013GraphQlResponse-aktoerId.json");
		postPdlGraphql(PDL_PERSONNAVN_HAPPY, OK.value());
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(containing("448212366"))
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(backoutQueue));
	}

	private void verifyLagreMetadataOgVideresendTilQdist015() {
		verifyPostSafJournalpost();
		verifyPostSafJournalpostLightweight();
		verifyPutOppdaterForsendelseWithMetadata();
		assertMessageOnQueue(qdist015);
	}

	private void verifyPostSafJournalpost() {
		verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safQdist013GraphQlRequest.json"))));
	}

	private void verifyPostSafJournalpostLightweight() {
		verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
	}

	private void verifyPutOppdaterForsendelseWithMetadata() {
		var requests = WireMock.findAll(putRequestedFor(urlEqualTo(OPPDATERFORSENDELSE_URL)));
		assertEquals(1, requests.size());

		LoggedRequest request = requests.getFirst();
		assertNotNull(request.getBodyAsString());
		JsonNode requestBody = parseJson(request.getBodyAsString());
		assertNotNull(requestBody.get("forsendelseMetadata"));
		assertEquals("DPO_AVTALEMELDING", requestBody.get("forsendelseMetadataType").asText());
	}

	private JsonNode parseJson(String body) {
		try {
			return OBJECT_MAPPER.readTree(body);
		} catch (Exception e) {
			throw new AssertionError("Kunne ikke parse oppdaterForsendelse-request som JSON", e);
		}
	}

	private void assertMessageOnQueue(Queue queue) {
		String message = receive(queue);
		assertNotNull(message);
		assertEquals(classpathToString("qdist013/qdist013-happy.xml"), message);
	}

	private String receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement<?> jaxbElement) {
			response = jaxbElement.getValue();
		}
		return (String) response;
	}

	private void sendStringMessage(Queue queue, final String message) {
		sendStringMessage(queue, message, callId);
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
