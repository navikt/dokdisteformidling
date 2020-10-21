package no.nav.dokdisteformidling.itest;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdisteformidling.itest.config.ApplicationTestConfig;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.apache.sshd.server.SshServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import org.xmlunit.assertj.XmlAssert;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.TKAT020_CACHE;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.TKAT021_CACHE;
import static no.nav.dokdisteformidling.itest.config.SftpConfig.startSshServer;
import static no.nav.dokdisteformidling.storage.S3Configuration.BUCKET_NAME;
import static no.nav.dokdisteformidling.testUtils.classpathToString;
import static no.nav.dokdisteformidling.testUtils.fileToString;
import static no.nav.dokdisteformidling.utils.DateConverterUtil.getNow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * @author Erik Bråten, Visma Consulting
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    public static final String DOCUMENT_FILESHARE = "dokumentdistribusjon/documentFileshare/";
    private static String CALL_ID;
    private static final String KONVERSASJON_ID = "601a9fcd-8bae-4076-a2d7-37f9dd17e050";
    private static final String BESTILLINGS_ID = "b8b297e1-46c1-4657-9f2d-7cf6dd089b9d";

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
        final Path documentFileshare = tempDir.resolve(DOCUMENT_FILESHARE);
        Files.createDirectories(documentFileshare);
        sshServer = startSshServer(tempDir);
        System.setProperty("sftp.privateKeyFile", new ClassPathResource("ssh/id_rsa").getURL().getPath());
        System.setProperty("sftp.port", Integer.toString(sshServer.getPort()));
    }

    @AfterAll
    public static void stopServer() throws Exception {
        sshServer.stop(true);
    }

    @BeforeEach
    public void setupBefore() throws IOException {
        FileUtils.cleanDirectory(tempDir.resolve(DOCUMENT_FILESHARE).toFile());
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
    @Order(Integer.MIN_VALUE)
    @Disabled
    public void shouldProcessNewForsendelse() throws Exception {

        stubGetForsendelse("__files/rdist001/getForsendelse-happy.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();
        stubPostSafJournalpost("saf/safGraphQlResponse-happy.json");
        stubGetSecurityToken();
        stubPutForsendelseStatusAndkonversasjonsId();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        String uploadFilePath = tempDir.toString() + REMOTE_FILE_PATH;
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String response = receive(tdist005);
            String expected = classpathToString("tdist005/tdist005_happy.xml");

            XmlAssert.assertThat(comparableMessage(response)).and(comparableMessage(expected))
                    .ignoreWhitespace()
                    .areSimilar();
        });

        assertThat(fileToString(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK + ".pdf"))).isEqualTo(HOVEDDOK_TEST_CONTENT);
        assertThat(fileToString(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 + ".pdf"))).isEqualTo(VEDLEGG1_TEST_CONTENT);
        assertThat(fileToString(new File(uploadFilePath + DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 + ".pdf"))).isEqualTo(VEDLEGG2_TEST_CONTENT);
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldProcessRForsendelseResendingWithoutCallId() throws Exception {

        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();
        stubPostSafJournalpost("saf/safGraphQlResponse-happy.json");
        stubGetSecurityToken();
        stubPutForsendelseStatusAndkonversasjonsId();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"), null);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String response = receive(tdist005);
            String expected = classpathToString("tdist005/tdist005_happy.xml");
            XmlAssert.assertThat(comparableMessage(response)).and(comparableMessage(expected))
                    .ignoreWhitespace()
                    .areSimilar();
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldProcessForsendelseResendingWithEmptyCallId() throws Exception {

        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();
        stubPostSafJournalpost("saf/safGraphQlResponse-happy.json");
        stubGetSecurityToken();
        stubPutForsendelseStatusAndkonversasjonsId();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"), "");

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String response = receive(tdist005);
            String expected = classpathToString("tdist005/tdist005_happy.xml");
            XmlAssert.assertThat(comparableMessage(response)).and(comparableMessage(expected))
                    .ignoreWhitespace()
                    .areSimilar();
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowForsendelseManglerForsendelseIdFunctionalExceptionManglerForsendelseId() throws Exception {
        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-feilId.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-feilId.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowForsendelseManglerForsendelseIdFunctionalExceptionTomForsendelseId() throws Exception {
        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-tom-forsendelseId.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-tom-forsendelseId.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowRdist001HentForsendelseFunctionalException() throws Exception {

        stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowRdist001HentForsendelseTechnicalException() throws Exception {
        stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowInvalidForsendelseStatusException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-oversendtForsendelseStatus.json");

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowDigitalKontaktinformasjonV1KontaktinformasjonIkkeFunnetFunctionalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(classpathToString("__files/digitalkontaktinformasjonv1/ikke-funnet.xml"))));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowDigitalKontaktinformasjonV1PersonIkkeFunnetFunctionalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(classpathToString("__files/digitalkontaktinformasjonv1/person-ikke-funnet.xml"))));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowDigitalKontaktinformasjonV1SikkerhetsbegrensingFunctionalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(classpathToString("__files/digitalkontaktinformasjonv1/sikkerhet.xml"))));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowDigitalKontaktinformasjonV1HentSikkerDigitalPostadresseTechnicalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubFor(post("/digitalkontaktinformasjonv1").willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(classpathToString("__files/digitalkontaktinformasjonv1/securityError.xml"))));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowTkat020FunctionalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK))
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowTkat020FunctionalExceptionUtenDokumentProduksjonsInfo() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-utenDokumentProduksjonsInfo.json");

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowTkat020FunctionalExceptionUtenDistribusjonInfo() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-utenDistribusjonInfo.json");

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowTkat020FunctionalExceptionUtenSDPDistribusjonVarsel() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-utenSDPDistribusjonVarsel.json");

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowTkat020TechicalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubFor(get(urlMatching("/dokumenttypeinfo/" + DOKUMENTTYPE_ID_HOVEDDOK))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowTkat021FunctionalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID))
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowTkat021TechnicalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubFor(get(urlMatching("/varselinfo/" + VARSEL_TYPE_ID))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowKunneIkkeDeserialisereS3PayloadFunctionalException() throws Exception {
        when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2))).thenReturn("notJsonSerializedString");

        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowS3FailedToGetDocumentTechnicalExceptionVedSdkClientException() throws Exception {
        when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK))).thenThrow(new SdkClientException("SdkClientException"));

        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowS3FailedToGetDocumentTechnicalExceptionVedSecurityException() throws Exception {
        when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK))).thenThrow(new SecurityException("SecurityException"));

        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    // Kjøres sist pga den stopper sftp serveren
    @Test
    @Order(Integer.MAX_VALUE)
    public void shouldGetErrorWhenSFTPNotAvailable() throws Exception {
        stopServer();

        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowSafFunctionalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();
        stubFor(post(urlMatching("/safgraphql"))
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
        stubGetSecurityToken();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldPutMessageOnBackoutQueueWhenSafJournalpostIkkeFunnetException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();
        stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody("")));
        stubGetSecurityToken();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowSafJournalpostValidationException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();
        stubPostSafJournalpost("saf/safGraphQlResponse-tommeTitler.json");
        stubGetSecurityToken();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowSafTechnicalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();
        stubFor(post(urlMatching("/safgraphql"))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
        stubGetSecurityToken();

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowStsTechnicalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();
        stubPostSafJournalpost("saf/safGraphQlResponse-happy.json");
        stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid")
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowRdist001OppdaterForsendelseStatusFunctionalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();
        stubPostSafJournalpost("saf/safGraphQlResponse-happy.json");
        stubGetSecurityToken();
        stubFor(put(urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + KONVERSASJON_ID))
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011FunksjonellFeilQueue = receive(qdist011FunksjonellFeil);
            assertNotNull(resultOnQdist011FunksjonellFeilQueue);
            assertEquals(resultOnQdist011FunksjonellFeilQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void shouldThrowRdist001OppdaterForsendelseStatusTechnicalException() throws Exception {
        stubGetForsendelse("__files/rdist001/getForsendelse-resending.json");
        stubPostDigitalKontaktInformasjon();
        stubGetDokumentTypeInfo("dokumentinfov4/tkat020-happy.json");
        stubGetVarselInfo();
        stubPostSafJournalpost("saf/safGraphQlResponse-happy.json");
        stubGetSecurityToken();
        stubFor(put(urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + KONVERSASJON_ID))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist011, classpathToString("qdist011/qdist011-happy.xml"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String resultOnQdist011BackoutQueue = receive(backoutQueue);
            assertNotNull(resultOnQdist011BackoutQueue);
            assertEquals(resultOnQdist011BackoutQueue, classpathToString("qdist011/qdist011-happy.xml"));
        });
    }

    private void stubPutForsendelseStatusAndkonversasjonsId() {
        stubFor(put(urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=OVERSENDT&konversasjonsId=" + KONVERSASJON_ID))
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
                .withBody(classpathToString(bodyClasspath))));
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

    private String comparableMessage(String melding) {
        // Tøm CreationDateAndTime og putt inn placeholders.
        return melding.replaceFirst("(?<=CreationDateAndTime>).+(?=</)", "")
                .replace("${bestillingsId}", BESTILLINGS_ID)
                .replace("${konversasjonId}", KONVERSASJON_ID);
    }
}



