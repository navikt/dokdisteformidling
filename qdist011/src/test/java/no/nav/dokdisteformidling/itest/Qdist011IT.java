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
import static no.nav.dokdisteformidling.common.FunctionalUtils.getNow;
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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdisteformidling.itest.config.ApplicationTestConfig;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final String HOVEDDOK_TEST_CONTENT = "HOVEDDOK_TEST_CONTENT";
	private static final String VEDLEGG1_TEST_CONTENT = "VEDLEGG1_TEST_CONTENT";
	private static final String VEDLEGG2_TEST_CONTENT = "VEDLEGG2_TEST_CONTENT";
	private static final String REMOTE_FILE_PATH = "/dokumentdistribusjon/documentFileshare/";
	private static String CALL_ID;
	private static String BESTILLINGS_ID;

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
		BESTILLINGS_ID = UUID.randomUUID().toString();

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

		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();
		stubPostSafJournalpost("saf/safGraphQlResponse-happy.json");
		stubGetSecurityToken();
		stubPutForsendelseStatusAndkonversasjonsId();

		sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

		String uploadFilePath = tempDir.toString() + REMOTE_FILE_PATH;
		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			assertTrue(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK + ".pdf").exists());
			assertTrue(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 + ".pdf").exists());
			assertTrue(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 + ".pdf").exists());

			String response = receive(tdist005);
			String expected = classpathToString("tdist005/tdist005-happy.xml").replace("insertBestillingsIdHere", BESTILLINGS_ID);
			assertEquals(comparableMessage(expected), comparableMessage(response));
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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowInvalidForsendelseStatusException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-oversendtForsendelseStatus.json");

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowDigitalKontaktinformasjonV1KontaktinformasjonIkkeFunnetFunctionalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowDigitalKontaktinformasjonV1PersonIkkeFunnetFunctionalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
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
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
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
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowTkat020FunctionalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowTkat020FunctionalExceptionUtenDokumentProduksjonsInfo() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-utenDokumentProduksjonsInfo.json");

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowTkat020FunctionalExceptionUtenDistribusjonInfo() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-utenDistribusjonInfo.json");

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowTkat020FunctionalExceptionUtenSDPDistribusjonVarsel() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-utenSDPDistribusjonVarsel.json");

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowTkat020TechicalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowTkat021FunctionalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowTkat021TechnicalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowKunneIkkeDeserialisereS3PayloadFunctionalException() throws Exception {
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2))).thenReturn("notJsonSerializedString");

		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowS3FailedToGetDocumentTechnicalExceptionVedSdkClientException() throws Exception {
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK))).thenThrow(new SdkClientException("SdkClientException"));

		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowS3FailedToGetDocumentTechnicalExceptionVedSecurityException() throws Exception {
		when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK))).thenThrow(new SecurityException("SecurityException"));

		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldGetErrorWhenSFTPNotAvailable() throws Exception {
		stopServer();

		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowSafFunctionalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
		stubGetSecurityToken();

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowSafJournalpostIkkeFunnetFunctionalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();
		stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
				.withBody("")));
		stubGetSecurityToken();

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowSafJournalpostValidationException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();
		stubPostSafJournalpost("saf/safGraphQlResponse-tommeTitler.json");
		stubGetSecurityToken();

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowSafTechnicalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
		stubGetSecurityToken();

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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowStsTechnicalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();
		stubPostSafJournalpost("saf/safGraphQlResponse-happy.json");
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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	@Test
	public void shouldThrowRdist001OppdaterForsendelseStatusFunctionalException() throws Exception {
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();
		stubPostSafJournalpost("saf/safGraphQlResponse-happy.json");
		stubGetSecurityToken();
		stubFor(put("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)
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
		stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
		stubPostDigitalKontaktInformasjon();
		stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
		stubGetVarselInfo();
		stubPostSafJournalpost("saf/safGraphQlResponse-happy.json");
		stubGetSecurityToken();
		stubFor(put("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)
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
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	private void stubPutForsendelseStatusAndkonversasjonsId() {
		stubFor(put("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));
	}

	private void stubGetSecurityToken() {
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse-happy.json")));
	}

	private void stubPostSafJournalpost(String bodyFileName) {
		stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
				.withBodyFile(bodyFileName)));
	}

	private void stubGetVarselInfo() {
		stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID)).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("varselinfov1/tkat021-happy.json")));
	}

	private void stubGetDokumentTypeInfo(String bodyFileName) {
		stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile(bodyFileName)));
	}

	private void stubPostDigitalKontaktInformasjon() throws IOException {
		stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBody(classpathToString("__files/digitalkontaktinformasjonv1/dki-happy.xml")
						.replace("insertDateHere", getNow().toString()))));
	}

	private void stubGetForsendelse(String bodyClasspath) throws IOException {
		stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody(classpathToString(bodyClasspath).replace("insertBestillingsIdHere", BESTILLINGS_ID))));
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

	private void verifyAllStubs(int count) {
		verify(count, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
		verify(count, postRequestedFor(urlEqualTo("/digitalkontaktinformasjonv1")));
		verify(count, getRequestedFor(urlEqualTo("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK)));
		verify(count, getRequestedFor(urlEqualTo("/varselinfo/" + VARSEL_TYPE_ID)));
		verify(count, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(count, postRequestedFor(urlEqualTo("/safgraphql")));
		verify(count, putRequestedFor(
				urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + BESTILLINGS_ID)));
	}

	private String comparableMessage(String melding) {
		Pattern pattern = Pattern.compile("(?<=ns[0-9]:sendDigitalPost).+(?=>)");
		Matcher matcher = pattern.matcher(melding);
		if (matcher.find()) {
			String namespaces = matcher.group();
			assertTrue(namespaces.contains("http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader"));
			assertTrue(namespaces.contains("http://begrep.difi.no/sdp/schema_v10"));
			assertTrue(namespaces.contains("http://nav.no/tjeneste/virksomhet/digitalpost/sendDigitalPost/v1"));
			assertTrue(namespaces.contains("http://www.w3.org/2000/09/xmldsig#"));
		} else {
			fail("Melding mangler sendDigitalPost element med namespaces");
		}

		// tøm CreationDateAndTime
		// fjern namespaces siden rekkefølgen kan variere
		return melding.replaceFirst("(?<=CreationDateAndTime>).+(?=</)", "")
				.replaceAll("ns[0-9]:", "")
				.replaceFirst("(?<=<sendDigitalPost).+(?=>)", "")
				.replaceAll("[\t\r\n ]", "");

	}
}



