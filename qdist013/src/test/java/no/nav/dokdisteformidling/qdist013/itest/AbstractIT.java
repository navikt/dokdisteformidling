package no.nav.dokdisteformidling.qdist013.itest;

import jakarta.jms.Queue;
import no.nav.dokdisteformidling.qdist013.itest.config.ApplicationTestConfig;
import org.apache.camel.CamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static no.nav.dokdisteformidling.constants.RouteConstants.QDIST013_SERVICE_ID;
import static no.nav.dokdisteformidling.qdist013.TestUtil.classpathToString;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles({"itest", "avtaltmelding_v2"})
public abstract class AbstractIT {

	protected static final String PDL_PERSONNAVN_HAPPY = "pdl/personnavn_happy.json";
	protected static final String PDL_FORNAVN_NULL = "pdl/personnavn_nullfornavn.json";
	protected static final String PDL_IDENT_NOT_FOUND = "pdl/pdlident_not_found.json";
	protected static final String PDL_IDENT_HAPPY = "pdl/pdlident_happy.json";

	protected static final String FORSENDELSE_ID = "33333";
	protected static final String EREG_URL = "/ereg/v2/organisasjon/990983666/noekkelinfo";
	protected static final String OPPDATERFORSENDELSE_URL = "/administrerforsendelse/oppdaterforsendelse";

	protected String callId;

	@Autowired
	@Qualifier("qdist013")
	protected Queue qdist013;

	@Autowired
	protected Queue qdist013FunksjonellFeil;

	@Autowired
	protected Queue backoutQueue;

	@Autowired
	@Qualifier("qdist015")
	protected Queue qdist015;

	@Autowired
	protected JmsTemplate jmsTemplate;

	@Autowired
	private CamelContext camelContext;

	@BeforeEach
	void setUpTest() throws Exception {
		callId = UUID.randomUUID().toString();
		// Stopp og start ruten for å tømme Camel-intern state (inflight exchanges) og WireMock-stubber mellom tester
		camelContext.getRouteController().stopRoute(QDIST013_SERVICE_ID);
		resetAllRequests();
		camelContext.getRouteController().startRoute(QDIST013_SERVICE_ID);
	}

	protected static void stubPutOppdaterForsendelse(HttpStatus status) {
		stubFor(put(urlMatching(OPPDATERFORSENDELSE_URL))
				.willReturn(aResponse()
						.withStatus(status.value())));
	}

	protected static void stubEreg() {
		stubFor(get(EREG_URL)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("ereg/happy.json")));
	}

	protected static void stubEreg(String fileName) {
		stubFor(get(EREG_URL)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(fileName)));
	}

	protected static void stubEreg(HttpStatus httpStatus) {
		stubFor(get(EREG_URL)
				.willReturn(aResponse()
						.withStatus(httpStatus.value())));
	}

	protected void stubGetForsendelse(String responsebody) {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToString(responsebody).replace("insertCallIdHere", callId))));
	}

	public static void postPdlGraphql(String filePath, int status) {
		stubFor(post(urlMatching("/pdl"))
				.willReturn(aResponse()
						.withStatus(status)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(filePath)));
	}

	protected static void stubPostSafJournalpost(String journalpostId, String returnBodyFileName) {
		stubFor(post(urlMatching("/safgraphql"))
				.withRequestBody(matchingJsonPath("$.variables[?(@.queryJournalpostId == '" + journalpostId + "')]"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(returnBodyFileName)));
	}

	protected static void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}
}
