package no.nav.dokdisteformidling.itest;

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
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.TKAT020_CACHE;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.TKAT021_CACHE;
import static no.nav.dokdisteformidling.constants.RetryConstants.MAX_ATTEMPTS_SHORT;
import static no.nav.dokdisteformidling.itest.config.SftpConfig.startSshServer;
import static no.nav.dokdisteformidling.storage.S3Configuration.BUCKET_NAME;
import static no.nav.dokdisteformidling.testUtils.classpathToString;
import static no.nav.dokdisteformidling.testUtils.fileToString;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdisteformidling.itest.config.ApplicationTestConfig;
import no.nav.dokdisteformidling.qdist011.Qdist011FunctionalUtils;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.apache.sshd.server.SshServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
public class Qdist011IT {

	private static final String FORSENDELSE_ID = "33333";
	private static final String DOKUMENTTYPE_ID_HOVEDDOK = "dokumenttypeIdHoveddok";
	private static final String VARSEL_TYPE_ID = "SDP_000004";
	private static final String DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK = "dokumentObjektReferanseHoveddok";
	private static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 = "dokumentObjektReferanseVedlegg1";
	private static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 = "dokumentObjektReferanseVedlegg2";
	private static final String DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK_CORRUPT = "dokumentObjektReferanseHoveddokCorrupt";
	private static final String HOVEDDOK_TEST_CONTENT = "HOVEDDOK_TEST_CONTENT";
	private static final String VEDLEGG1_TEST_CONTENT = "VEDLEGG1_TEST_CONTENT";
	private static final String VEDLEGG2_TEST_CONTENT = "VEDLEGG2_TEST_CONTENT";
	private static final String REMOTE_FILE_PATH = "/dokumentdistribusjon/documentFileshare/";
	private static String CALL_ID;

	@Inject
	private JmsTemplate jmsTemplate;

	@Inject
	private Queue qdist011;

	@Inject
	private Queue qdist011FunksjonellFeil;

	@Inject
	private Queue tdist005;

	@Inject
	private Queue backoutQueue;

	@Inject
	private AmazonS3 amazonS3;

	@Inject
	private CacheManager cacheManager;

	private static SshServer sshServer;

	@TempDir
	static Path tempDir;

	@BeforeAll
	public static void setupBeforeAll() throws IOException {
		sshServer = startSshServer(tempDir);
		System.setProperty("sftp.privateKeyFile", new ClassPathResource("ssh/id_rsa").getURL().getPath());
		System.setProperty("sftp.port", Integer.toString(sshServer.getPort()));
	}

	@AfterAll
	public static void stopServer() throws Exception {
		sshServer.stop(true);
	}

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

		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));
		stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
				.withBodyFile("saf/safGraphQlResponse-happy.json")));
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse-happy.json")));
		stubFor(put("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		String uploadFilePath = tempDir.toString() + REMOTE_FILE_PATH;
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertTrue(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK + ".pdf").exists());
			assertTrue(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 + ".pdf").exists());
			assertTrue(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 + ".pdf").exists());

			String response = receive(tdist005);
			String expected = classpathToString("tdist005/tdist005-happy.xml").replace("insertCallIdHere", CALL_ID);
			assertEquals(replaceCreationDateAndTime(expected), replaceCreationDateAndTime(response));
		});

		String hoveddokContent = fileToString(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK + ".pdf"));
		String vedlegg1Content = fileToString(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 + ".pdf"));
		String vedlegg2Content = fileToString(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 + ".pdf"));

		assertEquals(HOVEDDOK_TEST_CONTENT, hoveddokContent);
		assertEquals(VEDLEGG1_TEST_CONTENT, vedlegg1Content);
		assertEquals(VEDLEGG2_TEST_CONTENT, vedlegg2Content);

		verifyAllStubs(1);
	}

	@Test
	public void shouldThrowForsendelseManglerPaakrevdHeaderFunctionalExceptionManglerCallId() throws Exception {

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"), null);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verifyAllStubs(0);
	}

	@Test
	public void shouldThrowForsendelseManglerPaakrevdHeaderFunctionalExceptionTomCallId() throws Exception {

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"), "");

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verifyAllStubs(0);
	}

	@Test
	public void shouldThrowForsendelseManglerForsendelseIdFunctionalExceptionManglerForsendelseId() throws Exception {
		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-feilId.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-feilId.xml"));
		});

		verifyAllStubs(0);
	}

	@Test
	public void shouldThrowForsendelseManglerForsendelseIdFunctionalExceptionTomForsendelseId() throws Exception {
		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-tom-forsendelseId.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-tom-forsendelseId.xml"));
		});

		verifyAllStubs(0);
	}

	@Test
	public void shouldThrowRdist001HentForsendelseFunctionalException() throws Exception {

		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(0, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(0, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowRdist001HentForsendelseTechnicalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011BackoutQueue = receive(backoutQueue);
			assertNotNull(resultOnQdist011BackoutQueue);
			assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(0, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(0, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowInvalidForsendelseStatusException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-oversendtForsendelseStatus.json")
						.replace("insertCallIdHere", CALL_ID))));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(0, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(0, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowDigitalKontaktinformasjonV1KontaktinformasjonIkkeFunnetFunctionalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/ikke-funnet.xml"))));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(0, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowDigitalKontaktinformasjonV1PersonIkkeFunnetFunctionalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/person-ikke-funnet.xml"))));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(0, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowDigitalKontaktinformasjonV1SikkerhetsbegrensingFunctionalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/sikkerhet.xml"))));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(0, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowDigitalKontaktinformasjonV1HentSikkerDigitalPostadresseTechnicalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/securityError.xml"))));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011BackoutQueue = receive(backoutQueue);
			assertNotNull(resultOnQdist011BackoutQueue);
			assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(MAX_ATTEMPTS_SHORT, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(0, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowTkat020FunctionalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK))
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowTkat020FunctionalExceptionUtenDokumentProduksjonsInfo() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-utenDokumentProduksjonsInfo.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowTkat020FunctionalExceptionUtenDistribusjonInfo() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-utenDistribusjonInfo.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowTkat020FunctionalExceptionUtenSDPDistribusjonVarsel() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-utenSDPDistribusjonVarsel.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowTkat020TechicalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011BackoutQueue = receive(backoutQueue);
			assertNotNull(resultOnQdist011BackoutQueue);
			assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(0, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowTkat021FunctionalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID))
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowTkat021TechnicalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011BackoutQueue = receive(backoutQueue);
			assertNotNull(resultOnQdist011BackoutQueue);
			assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowKunneIkkeDeserialisereS3PayloadFunctionalException() throws Exception {
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2))).thenReturn("notJsonSerializedString");

		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowS3FailedToGetDocumentTechnicalExceptionVedSdkClientException() throws Exception {
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK))).thenThrow(new SdkClientException("SdkClientException"));

		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011BackoutQueue = receive(backoutQueue);
			assertNotNull(resultOnQdist011BackoutQueue);
			assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowS3FailedToGetDocumentTechnicalExceptionVedSecurityException() throws Exception {
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK))).thenThrow(new SecurityException("SecurityException"));

		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011BackoutQueue = receive(backoutQueue);
			assertNotNull(resultOnQdist011BackoutQueue);
			assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldGetErrorWhenSFTPNotAvailable() throws Exception {
		stopServer();

		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011BackoutQueue = receive(backoutQueue);
			assertNotNull(resultOnQdist011BackoutQueue);
			assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(0, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowSafFunctionalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse-happy.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(1, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(1, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowSafJournalpostIkkeFunnetFunctionalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));
		stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
				.withBody("")));
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse-happy.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(1, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(1, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowSafJournalpostValidationException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));
		stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
				.withBodyFile("saf/safGraphQlResponse-tommeTitler.json")));
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse-happy.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(1, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(1, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowSafTechnicalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse-happy.json")));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011BackoutQueue = receive(backoutQueue);
			assertNotNull(resultOnQdist011BackoutQueue);
			assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(MAX_ATTEMPTS_SHORT, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowStsTechnicalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));
		stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
				.withBodyFile("saf/safGraphQlResponse-happy.json")));
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011BackoutQueue = receive(backoutQueue);
			assertNotNull(resultOnQdist011BackoutQueue);
			assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(0, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(0, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	@Test
	public void shouldThrowRdist001OppdaterForsendelseStatusFunctionalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));
		stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
				.withBodyFile("saf/safGraphQlResponse-happy.json")));
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse-happy.json")));
		stubFor(put("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
			assertNotNull(resultOnQdist011FunksjonellFeilQueue);
			assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verifyAllStubs(1);
	}

	@Test
	public void shouldThrowRdist001OppdaterForsendelseStatusTechnicalException() throws Exception {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString("__files/rjoark001/getForsendelse-happy.json")
						.replace("insertCallIdHere", CALL_ID))));
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", Qdist011FunctionalUtils.getNow().toString()))));
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("dokumentinfov4/tkat020-happy.json")));
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));
		stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
				.withBodyFile("saf/safGraphQlResponse-happy.json")));
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse-happy.json")));
		stubFor(put("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			String resultOnQdist011BackoutQueue = receive(backoutQueue);
			assertNotNull(resultOnQdist011BackoutQueue);
			assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
		});

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(1, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(1, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(1, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(1, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(MAX_ATTEMPTS_SHORT, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	private void sendStringMessage(Queue queue, final String message) {
		sendStringMessage(queue, message, CALL_ID);
	}

	private void sendStringMessage(Queue queue, final String message, final String callId) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = new ActiveMQTextMessage();
			msg.setText(message);
			if (callId != null)
				msg.setStringProperty("callId", callId);
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

	private void verifyAllStubs(int count) {
		verify(count, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(count, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(count, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(count, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(count, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(count, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(count, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT")));
	}

	private String replaceCreationDateAndTime(String melding) {
		// erstatt dato (forventes å inneholde bindestrek) med tom streng
		return melding.replaceAll("[\t\r\n ]", "").replaceFirst("(?<=CreationDateAndTime>).+(?=</)", "");
	}
}



