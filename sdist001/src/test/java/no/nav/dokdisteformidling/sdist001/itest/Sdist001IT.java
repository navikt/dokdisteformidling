package no.nav.dokdisteformidling.sdist001.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
import static no.nav.dokdisteformidling.testUtils.classpathToString;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdisteformidling.sdist001.Sdist001Scheduled;
import no.nav.dokdisteformidling.sdist001.itest.config.ApplicationTestConfig;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
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
public class Sdist001IT {

	private static String CALL_ID;
	private static String HENT_EFORMIDLINGSFORSENDELSER_URL = "/administrerforsendelse/henteformidlingforsendelser";

	@Inject
	private Sdist001Scheduled sdist001Scheduled;

	@BeforeEach
	public void setupBefore() {
		CALL_ID = UUID.randomUUID().toString();

		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
	}

	@Test
	public void shouldHenteTomListeOk() throws Exception {
		stubFor(get(HENT_EFORMIDLINGSFORSENDELSER_URL).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-empty.json"))));

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(1, getRequestedFor(urlEqualTo(HENT_EFORMIDLINGSFORSENDELSER_URL)));
	}

	@Test
	public void shouldNotContactIntegrationPointWhenIllegalForsendelseStatus() throws Exception {
		stubFor(get(HENT_EFORMIDLINGSFORSENDELSER_URL).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-illegalStatus.json"))));

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(1, getRequestedFor(urlEqualTo(HENT_EFORMIDLINGSFORSENDELSER_URL)));
		verify(0, getRequestedFor(urlMatching("/integrasjonspunkt/.*")));
	}

	@Test
	public void shouldSetForsendelseStatusOversendtToFeilet() throws Exception {
		stubFor(get(HENT_EFORMIDLINGSFORSENDELSER_URL).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-oversendtStatus.json"))));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=101").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/integrasjonspunkt/getStatus-TTL_EXPIRED.json"))));
		stubFor(put("/administrerforsendelse?forsendelseId=1&forsendelseStatus=FEILET")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(1, getRequestedFor(urlEqualTo(HENT_EFORMIDLINGSFORSENDELSER_URL)));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=101")));
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=1&forsendelseStatus=FEILET")));
	}

	@Test
	public void shouldSetForsendelseStatusOversendtToBekreftet() throws Exception {
		stubFor(get(HENT_EFORMIDLINGSFORSENDELSER_URL).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-oversendtStatus.json"))));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=101").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/integrasjonspunkt/getStatus-SENDT.json"))));
		stubFor(put("/administrerforsendelse?forsendelseId=1&forsendelseStatus=BEKREFTET")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(1, getRequestedFor(urlEqualTo(HENT_EFORMIDLINGSFORSENDELSER_URL)));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=101")));
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=1&forsendelseStatus=BEKREFTET")));
	}

	@Test
	public void shouldSetForsendelseStatusOversendtToEkspedert() throws Exception {
		stubFor(get(HENT_EFORMIDLINGSFORSENDELSER_URL).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-oversendtStatus.json"))));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=101").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/integrasjonspunkt/getStatus-MOTTATT.json"))));
		stubFor(get("/administrerforsendelse/1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/getForsendelse-BEKREFTET.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/juridiskLogg").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody("{\"id\": \"123\"}")));
		stubFor(put("/administrerforsendelse?forsendelseId=1&forsendelseStatus=EKSPEDERT")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(1, getRequestedFor(urlEqualTo(HENT_EFORMIDLINGSFORSENDELSER_URL)));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=101")));
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/1")));
		verify(1, postRequestedFor(urlEqualTo("/juridiskLogg")));
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=1&forsendelseStatus=EKSPEDERT")));
	}

	@Test
	public void shouldSetForsendelseStatusBekreftetToFeilet() throws Exception {
		stubFor(get(HENT_EFORMIDLINGSFORSENDELSER_URL).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-bekreftetStatus.json"))));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=101").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/integrasjonspunkt/getStatus-TTL_EXPIRED.json"))));
		stubFor(put("/administrerforsendelse?forsendelseId=1&forsendelseStatus=FEILET")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(1, getRequestedFor(urlEqualTo(HENT_EFORMIDLINGSFORSENDELSER_URL)));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=101")));
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=1&forsendelseStatus=FEILET")));
	}

	@Test
	public void shouldSetForsendelseStatusBekreftetToEkspedert() throws Exception {
		stubFor(get(HENT_EFORMIDLINGSFORSENDELSER_URL).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-bekreftetStatus3.json"))));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=101").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/integrasjonspunkt/getStatus-MOTTATT.json"))));
		stubFor(get("/administrerforsendelse/1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/getForsendelse-BEKREFTET.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(put("/administrerforsendelse?forsendelseId=1&forsendelseStatus=EKSPEDERT")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=102").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/integrasjonspunkt/getStatus-LEVERT.json"))));
		stubFor(get("/administrerforsendelse/2").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/getForsendelse-BEKREFTET.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(put("/administrerforsendelse?forsendelseId=2&forsendelseStatus=EKSPEDERT")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=103").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/integrasjonspunkt/getStatus-LEST.json"))));
		stubFor(get("/administrerforsendelse/3").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/getForsendelse-BEKREFTET.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(put("/administrerforsendelse?forsendelseId=3&forsendelseStatus=EKSPEDERT")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
		stubFor(post("/juridiskLogg").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody("{\"id\": \"123\"}")));

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(1, getRequestedFor(urlEqualTo(HENT_EFORMIDLINGSFORSENDELSER_URL)));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=101")));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=102")));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=103")));
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/1")));
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/2")));
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/3")));
		verify(3, postRequestedFor(urlEqualTo("/juridiskLogg")));
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=1&forsendelseStatus=EKSPEDERT")));
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=2&forsendelseStatus=EKSPEDERT")));
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=3&forsendelseStatus=EKSPEDERT")));
	}

	@Test
	public void shouldProcessAllForsendelserWhenFunctionalException() throws Exception {
		stubFor(get(HENT_EFORMIDLINGSFORSENDELSER_URL).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-bekreftetStatus2.json"))));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=101").willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=102").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/integrasjonspunkt/getStatus-MOTTATT.json"))));
		stubFor(get("/administrerforsendelse/2").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/getForsendelse-BEKREFTET.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(put("/administrerforsendelse?forsendelseId=2&forsendelseStatus=EKSPEDERT")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
		stubFor(post("/juridiskLogg").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody("{\"id\": \"123\"}")));

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(1, getRequestedFor(urlEqualTo(HENT_EFORMIDLINGSFORSENDELSER_URL)));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=101")));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=102")));
		verify(0, getRequestedFor(urlEqualTo("/administrerforsendelse/1")));
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/2")));
		verify(1, postRequestedFor(urlEqualTo("/juridiskLogg")));
		verify(0, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=1&forsendelseStatus=EKSPEDERT")));
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=2&forsendelseStatus=EKSPEDERT")));
	}

	@Test
	public void shouldStopProcessingWhenTechnicalException() throws Exception {
		stubFor(get(HENT_EFORMIDLINGSFORSENDELSER_URL).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-bekreftetStatus2.json"))));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=101")
				.willReturn(aResponse().withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())));

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(1, getRequestedFor(urlEqualTo(HENT_EFORMIDLINGSFORSENDELSER_URL)));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=101")));
		verify(0, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=102")));
		verify(0, getRequestedFor(urlEqualTo("/administrerforsendelse/1")));
		verify(0, getRequestedFor(urlEqualTo("/administrerforsendelse/2")));
		verify(0, postRequestedFor(urlEqualTo("/juridiskLogg")));
		verify(0, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=1&forsendelseStatus=EKSPEDERT")));
		verify(0, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=2&forsendelseStatus=EKSPEDERT")));
	}

	@Test
	public void shouldUseLatestIntegrasjonspunktStatus() throws Exception {
		stubFor(get(HENT_EFORMIDLINGSFORSENDELSER_URL).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-bekreftetStatus.json"))));
		stubFor(get("/integrasjonspunkt/api/statuses?conversationId=101").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/integrasjonspunkt/getStatus-flere.json"))));
		stubFor(get("/administrerforsendelse/1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rdist001/getForsendelse-BEKREFTET.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(put("/administrerforsendelse?forsendelseId=1&forsendelseStatus=EKSPEDERT")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
		stubFor(post("/juridiskLogg").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody("{\"id\": \"123\"}")));

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(1, getRequestedFor(urlEqualTo(HENT_EFORMIDLINGSFORSENDELSER_URL)));
		verify(1, getRequestedFor(urlEqualTo("/integrasjonspunkt/api/statuses?conversationId=101")));
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/1")));
		verify(1, postRequestedFor(urlEqualTo("/juridiskLogg")));
		verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=1&forsendelseStatus=EKSPEDERT")));
	}
}
