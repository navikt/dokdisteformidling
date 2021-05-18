package no.nav.dokdisteformidling.qdist013.itest;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import no.altinn.brokerserviceexternal.InitiateBrokerService;
import no.altinn.brokerserviceexternal.Manifest;
import no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import no.nav.dokdisteformidling.qdist013.itest.config.ApplicationTestConfig;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
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
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.dokdisteformidling.constants.RetryConstants.MAX_ATTEMPTS_SHORT;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static no.nav.dokdisteformidling.qdist013.TestUtil.classpathToString;
import static no.nav.dokdisteformidling.storage.S3Configuration.BUCKET_NAME;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static wiremock.com.google.common.base.Strings.isNullOrEmpty;


/**
 * @author Tsigab Angosom Gebremedhin , NAV
 */

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
class Qdist013ForAltinnIT {
    private static final String FORSENDELSE_ID = "33333";
    private static final String DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK = "dokumentObjektReferanseHoveddok";
    private static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 = "dokumentObjektReferanseVedlegg1";
    private static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 = "dokumentObjektReferanseVedlegg2";
    private static final String HOVEDDOK_TEST_CONTENT = "HOVEDDOK_TEST_CONTENT";
    private static final String VEDLEGG1_TEST_CONTENT = "VEDLEGG1_TEST_CONTENT";
    private static final String VEDLEGG2_TEST_CONTENT = "VEDLEGG2_TEST_CONTENT";
    private static final String BEARER_OIDC_TOKEN = "Bearer eyJraWQiOiIyZDYwNjZmNi1mM2ViLTRlYzktYjRlZS0wMzM1Nzg0MDY3MTMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZqb2Fya2FkbWluIiwiYXVkIjpbInNydmpvYXJrYWRtaW4iLCJwcmVwcm9kLmxvY2FsIl0sInZlciI6IjEuMCIsIm5iZiI6MTU1NjI2Nzg0NiwiYXpwIjoic3J2am9hcmthZG1pbiIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE1NTYyNjc4NDYsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTU1NjI3MTQ0NiwiaWF0IjoxNTU2MjY3ODQ2LCJqdGkiOiI5NzVmMjY4YS00ZmI3LTQ2NWMtOTIyZS0xY2Q4OTNjZDEwY2QifQ.e7e1cKmLt0wYSBdURju0pZnplheXl-T5Df7t2QKcOWpKfERKgfSnMOHPYuS80GJbwvfZXE7F_WiTyB2Klsv_shS2Iy_DqqS2qRPUit4fCDyXX4TMBVWWqBY60Wg46NuZGz4kje6z0BcT84cyrQSPKNuVEmy9xcdIXrQ2xzJy9NyOseSvEkUPX4Xj4yfCh6CoEIOsNDQ-hW6XUkbAKjF3nkM6AwSQ2cZTi9T7j12LNw4RQyBwl9PINP8d3t2jeOJ8Gq7xVkzlyL60SHH2UnblBag0UhCYLYIzuSr1lkpvZ_8q5vqg9DXk7CQZGmZNfoOOQsy1pBTyzU3JjhGmBNWZEg";

    @Value("${altinn.brokerserviceexternal.endpointurl}")
    private String brokerserviceexternalUrl;

    private static String CALL_ID;
    @Inject
    private Queue qdist013;

    @Inject
    private Queue qdist013FunksjonellFeil;
    @Inject
    private Queue backoutQueue;
    @Inject
    private JmsTemplate jmsTemplate;

    @Inject
    private AmazonS3 amazonS3;


    @Inject
    private EformidlingMottakerInfoService eformidlingMottakerInfoService;


    @BeforeEach
    public void setUp() {
        CALL_ID = UUID.randomUUID().toString();

        WireMock.reset();
        WireMock.resetAllRequests();
        WireMock.removeAllMappings();
        Mockito.reset(amazonS3);

        when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK)))
                .thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(HOVEDDOK_TEST_CONTENT.getBytes()).build()));
        when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1)))
                .thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(VEDLEGG1_TEST_CONTENT.getBytes()).build()));
        when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2)))
                .thenReturn(JsonSerializer.serialize(DokdistDokument.builder().pdf(VEDLEGG2_TEST_CONTENT.getBytes()).build()));


    }


    @Test
    void shouldHenteMottakerInfoFraServiceRegistery() {
        stubGetSecurityToken();
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();

        assertThat(mottakerInfo.getOrgnummer(), is(TRYGDERETTEN_ORGNUMMER));
        assertThat(mottakerInfo.getServiceCode(), is("4192"));
        assertThat(mottakerInfo.getServiceEditionCode(), is("270815"));
        verify(1, getRequestedFor(urlEqualTo("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + EformidlingConstants.AVTALTMELDING_PROCESS)));
        verify(1, postRequestedFor(urlEqualTo("/maskinporten")));

    }

    @Test
    void shouldIntiateBrokerService() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetTpsHentPersonNavn("20026900817");
        stubGetAktoerregisterHentIdentForAktoerId("1000045110509");
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, SECONDS).untilAsserted(() -> {
            verifyIntiateBrokerServiceStubs("", "20026900817", "1000045110509", 22, 1, 2);
        });


    }

    @Test
    void whenLightweightSafDataJournalfoertErNull() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-datojournalfoert-null.json");
        stubGetTpsHentPersonNavn("20026900817");
        stubGetAktoerregisterHentIdentForAktoerId("1000045110509");
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, SECONDS).untilAsserted(() -> {
            verifyIntiateBrokerServiceStubs("", "20026900817", "1000045110509", 18, 1, 2);
        });
    }

    @Test
    void shouldThrowExceptionWhenDatoJournalfoertErNullInJpQdist013() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-relevantdato-null.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-datojournalfoert-null.json");

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(qdist013FunksjonellFeil);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(1);
        verifyPostSafJournalpost();
    }

    @Test
    void brokerserviceStreamedShouldUploadFileToAltinn() {

        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetTpsHentPersonNavn("20026900817");
        stubGetAktoerregisterHentIdentForAktoerId("1000045110509");
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        stubUploadBrokerServiceStreamed();
        stubPostJuridiskLoggLagre();
        stubPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId();

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, SECONDS).untilAsserted(() -> {
            verifyIntiateBrokerServiceStubs("", "20026900817", "1000045110509", 22, 1, 2);
        });


    }

    @Test
    void shouldProcessForsendelseWithFnr() {

        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-fnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetTpsHentPersonNavn("01010012345");
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        stubUploadBrokerServiceStreamed();
        stubPostJuridiskLoggLagre();
        stubPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId();

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, SECONDS).untilAsserted(() -> {
            verifyAllStubs("", "01010012345", "", 20, 1);
        });

    }

    @Test
    void shouldProcessForsendelseWithOrgnr() {

        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetEregHentOrgNavn("123456789");
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        stubUploadBrokerServiceStreamed();
        stubPostJuridiskLoggLagre();
        stubPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId();

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, SECONDS).untilAsserted(() -> {
            verifyAllStubs("123456789", "", "", 19, 1);
        });

    }

    @Test
    void shouldThrowRdist001HentForsendelseFunctionalException() {
        stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(qdist013FunksjonellFeil);
        });

        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
    }

    @Test
    void shouldThrowS3FailedToGetDocumentTechnicalExceptionVedSdkClientException() {
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
    void shouldThrowS3FailedToGetDocumentTechnicalExceptionVedSecurityException() {
        when(amazonS3.getObjectAsString(eq(BUCKET_NAME), eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK))).thenThrow(new SecurityException("SecurityException"));

        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
    }

    @Test
    void shouldThrowStsTechnicalException() {
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
    void shouldPutOnBackoutQueueWhenSafJournalpostIkkeFunnetException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubFor(post(urlMatching("/safgraphql")).willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody("")));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(MAX_ATTEMPTS_SHORT);
        verify(MAX_ATTEMPTS_SHORT, postRequestedFor(urlEqualTo("/safgraphql")));
    }

    @Test
    void shouldThrowSafJournalpostQueryUnauthorizedException() {
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
    void shouldThrowSafJournalpostQueryTechnicalException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubFor(post(urlMatching("/safgraphql"))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verify(3, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
        verify(3, postRequestedFor(urlEqualTo("/safgraphql")));
    }

    @Test
    void shouldThrowSafJournalpostValidationException() {
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
    void shouldPutMessageOnBackoutWhenSafJournalpostIkkeFunnetExceptionUsingLightweightService() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
        stubGetAktoerregisterHentIdentForAktoerId("1000045110509");
        stubGetTpsHentPersonNavn("20026900817");
        stubFor(post(urlMatching("/safgraphql"))
                .withRequestBody(containing("queryJournalpostId\":\"448212366\""))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody("")));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(MAX_ATTEMPTS_SHORT + 4);
        verifyPostSafJournalpost();
        verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
                .withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
    }

    @Test
    void shouldThrowSafJournalpostQueryUnauthorizedExceptionLightweight() {
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
    void shouldThrowSafJournalpostQueryTechnicalExceptionLightweight() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
        stubGetAktoerregisterHentIdentForAktoerId("1000045110509");
        stubGetTpsHentPersonNavn("20026900817");
        stubFor(post(urlMatching("/safgraphql"))
                .withRequestBody(containing("queryJournalpostId\":\"448212366\""))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(4 + MAX_ATTEMPTS_SHORT);
        verifyPostSafJournalpost();
        verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
                .withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
    }


    @Test
    void shouldThrowAktoerHentIdentForAktoerIdFunctionalException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
                .withHeader("Nav-Personidenter", equalTo("1000045110509"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
                .willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        stubUploadBrokerServiceStreamed();
        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(qdist013FunksjonellFeil);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(1 + 1);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
                .withHeader("Nav-Personidenter", equalTo("1000045110509"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
    }

    @Test
    void shouldThrowAktoerHentIdentForAktoerIdFunctionalExceptionIngenResponse() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        String nullStr = null;
        stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
                .withHeader("Nav-Personidenter", equalTo("1000045110509"))
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
        verifyGetSecurityToken(2);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
                .withHeader("Nav-Personidenter", equalTo("1000045110509"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
    }

    @Test
    void shouldThrowAktoerHentIdentForAktoerIdFunctionalExceptionFeilmelding() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
                .withHeader("Nav-Personidenter", equalTo("1000045110509"))
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
        verifyGetSecurityToken(1 + 1);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
                .withHeader("Nav-Personidenter", equalTo("1000045110509"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
    }

    @Test
    void shouldThrowAktoerHentIdentForAktoerIdFunctionalExceptionIngenIdent() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
                .withHeader("Nav-Personidenter", equalTo("1000045110509"))
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
        verifyGetSecurityToken(2);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
                .withHeader("Nav-Personidenter", equalTo("1000045110509"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
    }

    @Test
    void shouldThrowAktoerHentIdentForAktoerIdTechnicalException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-aktoerId.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubFor(get("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent")
                .withHeader("Nav-Personidenter", equalTo("1000045110509"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(1 + MAX_ATTEMPTS_SHORT);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
                .withHeader("Nav-Personidenter", equalTo("1000045110509"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
    }

    @Test
    void shouldThrowTpsHentNavnFunctionalException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-fnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubFor(get("/tps/v1/navn")
                .withHeader("Nav-Personident", equalTo("01010012345"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(qdist013FunksjonellFeil);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(1 + 1);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(1, getRequestedFor(urlEqualTo("/tps/v1/navn"))
                .withHeader("Nav-Personident", equalTo("01010012345"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
    }

    @Test
    void shouldThrowTpsHentNavnTechnicalException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-fnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubFor(get("/tps/v1/navn")
                .withHeader("Nav-Personident", equalTo("01010012345"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(1 + MAX_ATTEMPTS_SHORT);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/tps/v1/navn"))
                .withHeader("Nav-Personident", equalTo("01010012345"))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
    }

    @Test
    void shouldThrowEregHentNoekkelinfoFunctionalException() {
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
        verifyGetSecurityToken(1);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(1, getRequestedFor(urlEqualTo("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo")));
    }

    @Test
    void shouldThrowEregHentNoekkelinfoFunctionalExceptionIngenResponse() {
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
        verifyGetSecurityToken(1);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(1, getRequestedFor(urlEqualTo("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo")));
    }

    @Test
    void shouldThrowEregHentNoekkelinfoFunctionalExceptionManglerNavn() {
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
        verifyGetSecurityToken(1);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(1, getRequestedFor(urlEqualTo("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo")));
    }

    @Test
    void shouldThrowEregHentNoekkelinfoTechnicalException() {
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
        verifyGetSecurityToken(1);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/ereg/v1/organisasjon/" + "123456789" + "/noekkelinfo")));
    }


    @Test
    @Disabled
    void altinnUploadFileshouldThrowTechnicalExceptionLast() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetEregHentOrgNavn("123456789");
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        stubFor(post(urlMatching("brokerserviceexternalstreamed/upload"))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(15);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verifyGetEregHentOrgNavn("123456789");
        verifyPostMaskinporten();
        verifyGetServiceRegistry();
        verifyPostIntiateBrokerService();

    }

    @Test
    void serviceRegistryShouldThrowTechnicalExceptionAndRetry() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetEregHentOrgNavn("123456789");
        stubPostMaskinporten();
        stubFor(get(urlMatching("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + EformidlingConstants.AVTALTMELDING_PROCESS))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        // Retry 3x5s i serviceregistryconsumer
        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(19);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verifyGetEregHentOrgNavn("123456789");
        verifyPostMaskinporten(3);
        verifyGetServiceRegistry(3);
    }

    @Test
    void shouldThrowInitiateBrokerServiceTechnicalException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetEregHentOrgNavn("123456789");
        stubPostMaskinporten();
        stubGetServiceRegistry();

        stubFor(post(urlMatching("/brokerserviceexternal"))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML_VALUE)));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(19);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verifyGetEregHentOrgNavn("123456789");
        verifyPostMaskinporten();
        verifyGetServiceRegistry();
        verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternal")));

    }

    @Test
    @Disabled
    void shouldThrowLagreJuridiskLoggFunctionalException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetEregHentOrgNavn("123456789");
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        stubUploadBrokerServiceStreamed();
        stubFor(post(urlMatching("/juridisklogg.*"))
                .willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(qdist013FunksjonellFeil);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(15);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verifyGetEregHentOrgNavn("123456789");
        verifyPostIntiateBrokerService();
        verifyPostMaskinporten();
        verifyGetServiceRegistry();
        verifyPostUploadBrokerServiceStreamed();

        verify(1, postRequestedFor(urlEqualTo("/juridisklogg"))
                .withRequestBody(equalToJson(classpathToString("__files/juridisklogg/juridiskloggRequest.json"), true, true)));
    }

    @Test
    void shouldThrowLagreJuridiskLoggTechnicalException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetEregHentOrgNavn("123456789");
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        stubUploadBrokerServiceStreamed();
        stubFor(post(urlMatching("/juridisklogg.*"))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(19);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verifyGetEregHentOrgNavn("123456789");
        verifyPostIntiateBrokerService();
        verifyPostMaskinporten();
        verifyGetServiceRegistry();
        verifyPostUploadBrokerServiceStreamed();

        verify(MAX_ATTEMPTS_SHORT, postRequestedFor(urlEqualTo("/juridisklogg"))
                .withRequestBody(equalToJson(classpathToString("__files/juridisklogg/juridiskloggRequest.json"), true, true)));
    }

    @Test
    void shouldThrowRdist001OppdaterForsendelseFunctionalException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetEregHentOrgNavn("123456789");
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        stubUploadBrokerServiceStreamed();
        stubPostJuridiskLoggLagre();
        stubFor(put(urlMatching("/administrerforsendelse\\?forsendelseId=" + FORSENDELSE_ID + "\\&forsendelseStatus=OVERSENDT\\&konversasjonsId=.*"))
                .willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(qdist013FunksjonellFeil);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(19);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verifyGetEregHentOrgNavn("123456789");
        verifyPostMaskinporten();
        verifyGetServiceRegistry();
        verifyPostIntiateBrokerService();
        verifyPostUploadBrokerServiceStreamed();
        verifyPostJuridiskLoggLagre();
        String conversationId = findConversationId();
        verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID +
                "&forsendelseStatus=OVERSENDT&konversasjonsId=" + conversationId)));
    }

    @Test
    void shouldThrowRdist001OppdaterForsendelseTechnicalException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");
        stubGetSecurityToken();
        stubPostSafJournalpost("queryJournalpostId\":\"123\"", "saf/safQdist013GraphQlResponse-orgnr.json");
        stubPostSafJournalpost("queryJournalpostId\":\"448212366\"", "saf/safLightweightGraphQlResponse-happy.json");
        stubGetEregHentOrgNavn("123456789");
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubPostIntiateBrokerService();
        stubUploadBrokerServiceStreamed();
        stubPostJuridiskLoggLagre();
        stubFor(put(urlMatching("/administrerforsendelse\\?forsendelseId=" + FORSENDELSE_ID + "\\&forsendelseStatus=OVERSENDT\\&konversasjonsId=.*"))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verifyGetForsendelse();
        verifyGetSecurityToken(19);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(1);
        verifyGetEregHentOrgNavn("123456789");
        stubPostIntiateBrokerService();

        verifyPostJuridiskLoggLagre();
        String conversationId = findConversationId();
        verify(MAX_ATTEMPTS_SHORT, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID +
                "&forsendelseStatus=OVERSENDT&konversasjonsId=" + conversationId)));
    }

    @Test
    void shouldThrowRdist001HentForsendelseTechnicalException() {
        stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID)
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(backoutQueue);
        });

        verify(MAX_ATTEMPTS_SHORT, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
    }

    @Test
    void shouldThrowInvalidForsendelseStatusException() {
        stubGetForsendelse("__files/rjoark001/getForsendelse-oversendtForsendelseStatus.json");

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(qdist013FunksjonellFeil);
        });

        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
    }

    @Test
    void shouldThrowKunneIkkeDeserialisereS3PayloadFunctionalException() {
        when(amazonS3.getObjectAsString(eq(BUCKET_NAME),
                eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2))).thenReturn("notJsonSerializedString");

        stubGetForsendelse("__files/rjoark001/getForsendelse-happy.json");

        sendStringMessage(qdist013, classpathToString("qdist013/qdist013-happy.xml"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertMessageOnQueue(qdist013FunksjonellFeil);
        });

        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/" + FORSENDELSE_ID)));
    }


    private void verifyAllStubsSaf(String orgnr, String fnr, String aktoerId, int stsCount) {
        verifyGetForsendelse();
        verifyGetSecurityToken(stsCount);
        verifyPostSafJournalpost();
        verify(14, postRequestedFor(urlEqualTo("/safgraphql"))
                .withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));

        if (!isNullOrEmpty(orgnr)) {
            verifyGetEregHentOrgNavn(orgnr);
        } else {
            verifyGetTpsHentPersonNavn(fnr);
        }
        if (!isNullOrEmpty(aktoerId)) {
            verifyGetAktoerregisterHentIdentForAktoerId(aktoerId);
        }

        verifyPostMaskinporten();
        verifyGetServiceRegistry();
        verifyPostIntiateBrokerService();

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

        verifyPostMaskinporten();
        verifyGetServiceRegistry();
        verifyPostIntiateBrokerService();
        verifyPostUploadBrokerServiceStreamed();

        verifyPostJuridiskLoggLagre();
        String conversationId = findConversationId();
        verifyPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId(conversationId);

    }

    private void verifyAllStubs(String orgnr, String fnr, String aktoerId, int stsCount, int safCount) {
        verifyGetForsendelse();
        verifyGetSecurityToken(stsCount);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(safCount);
        if (!isNullOrEmpty(orgnr)) {
            verifyGetEregHentOrgNavn(orgnr);
        } else {
            verifyGetTpsHentPersonNavn(fnr);
        }
        if (!isNullOrEmpty(aktoerId)) {
            verifyGetAktoerregisterHentIdentForAktoerId(aktoerId);
        }

        verifyPostMaskinporten();
        verifyGetServiceRegistry();
        verifyPostIntiateBrokerService();
        verifyPostUploadBrokerServiceStreamed();

        verifyPostJuridiskLoggLagre();
        String conversationId = findConversationId();
        verifyPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId(conversationId);

    }

    private void verifyIntiateBrokerServiceStubs(String orgnr, String fnr, String aktoerId, int stsCount, int safCount, int akoterCount) {
        verifyGetForsendelse();
        verifyGetSecurityToken(stsCount);
        verifyPostSafJournalpost();
        verifyPostSafJournalpostLightweight(safCount);
        if (!isNullOrEmpty(orgnr)) {
            verifyGetEregHentOrgNavn(orgnr);
        } else {
            verifyGetTpsHentPersonNavn(fnr);
        }
        if (!isNullOrEmpty(aktoerId)) {
            verifyGetAktoerregisterHentIdentForAktoerId(aktoerId, akoterCount);
        }

        verifyPostMaskinporten();
        verifyGetServiceRegistry();
        verifyPostIntiateBrokerService();

    }


    private void verifyGetTpsHentPersonNavn(String fnr) {
        verify(1, getRequestedFor(urlEqualTo("/tps/v1/navn"))
                .withHeader("Nav-Personident", equalTo(fnr))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
    }

    private void verifyGetAktoerregisterHentIdentForAktoerId(String aktoerId) {
        verify(1, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
                .withHeader("Nav-Personidenter", equalTo(aktoerId))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
    }

    private void verifyGetAktoerregisterHentIdentForAktoerId(String aktoerId, int aktoerCount) {
        verify(aktoerCount, getRequestedFor(urlEqualTo("/aktoerregister/identer?gjeldende=true&identgruppe=NorskIdent"))
                .withHeader("Nav-Personidenter", equalTo(aktoerId))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN)));
    }

    private void verifyGetEregHentOrgNavn(String orgnr) {
        verify(1, getRequestedFor(urlEqualTo("/ereg/v1/organisasjon/" + orgnr + "/noekkelinfo")));
    }

    private void verifyPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId(String conversationId) {
        verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID +
                "&forsendelseStatus=OVERSENDT&konversasjonsId=" + conversationId)));
    }

    private void verifyPostJuridiskLoggLagre() {
        verify(1, postRequestedFor(urlEqualTo("/juridisklogg"))
                .withRequestBody(equalToJson(classpathToString("__files/juridisklogg/juridiskloggRequest.json"), true, true))
                .withRequestBody(matchingJsonPath("$.meldingsId", containing(CALL_ID)))
                .withRequestBody(matchingJsonPath("$.meldingsInnhold")));
    }

    private void verifyGetForsendelse() {
        verify(1, getRequestedFor(urlMatching("/administrerforsendelse/" + FORSENDELSE_ID)));
    }

    private void verifyGetSecurityToken(int stsCount) {
        verify(stsCount, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
    }

    private void verifyPostSafJournalpost() {
        verify(1, postRequestedFor(urlEqualTo("/safgraphql"))
                .withRequestBody(equalToJson(classpathToString("__files/saf/safQdist013GraphQlRequest.json"))));
    }


    private void verifyPostSafJournalpostLightweight(int count) {
        // no caching
        verify(count, postRequestedFor(urlEqualTo("/safgraphql"))
                .withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
    }

    private void verifyPostSafJournalpostLightweight() {
        // no caching
        verify(16, postRequestedFor(urlEqualTo("/safgraphql"))
                .withRequestBody(equalToJson(classpathToString("__files/saf/safLightweightGraphQlRequest.json"))));
    }

    private void verifyPostMaskinporten() {
        verify(1, postRequestedFor(urlEqualTo("/maskinporten")));
    }

    private void verifyPostMaskinporten(int expectedCount) {
        verify(expectedCount, postRequestedFor(urlEqualTo("/maskinporten")));
    }

    private void verifyPostIntiateBrokerService() {
        verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternal")));
    }

    private void verifyPostUploadBrokerServiceStreamed() {
        verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternalstreamed/upload")));
    }

    private void verifyGetServiceRegistry() {
        verify(1, getRequestedFor(urlEqualTo("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + EformidlingConstants.AVTALTMELDING_PROCESS)));
    }

    private void verifyGetServiceRegistry(int expectedCount) {
        verify(expectedCount, getRequestedFor(urlEqualTo("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + EformidlingConstants.AVTALTMELDING_PROCESS)));
    }

    private void assertMessageOnQueue(javax.jms.Queue queue) {
        String message = receive(queue);
        assertNotNull(message);
        assertEquals(message, classpathToString("qdist013/qdist013-happy.xml"));
    }

    @SuppressWarnings("unchecked")
    private <T> T receive(javax.jms.Queue queue) {
        Object response = jmsTemplate.receiveAndConvert(queue);
        if (response instanceof JAXBElement) {
            response = ((JAXBElement) response).getValue();
        }
        return (T) response;
    }

    private void stubPostIntiateBrokerService() {
        stubFor(post(urlMatching("/brokerserviceexternal"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML_VALUE)
                        .withBody(classpathToString("__files/altinn/brokerserviceinit_happy_response.xml").replace("localurl", brokerserviceexternalUrl))));
    }

    private void stubUploadBrokerServiceStreamed() {
        stubFor(post(urlMatching("/brokerserviceexternalstreamed/upload"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML_VALUE)
                        .withBody(classpathToString("__files/altinn/brokerserviceupload_happy_response.xml"))));
    }

    private void stubGetTpsHentPersonNavn(String fnr) {
        stubFor(get("/tps/v1/navn")
                .withHeader("Nav-Personident", equalTo(fnr))
                .withHeader(AUTHORIZATION, equalTo(BEARER_OIDC_TOKEN))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                        .withBodyFile("tps/tpsHentNavn_happy.json")));
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

    private void stubPostJuridiskLoggLagre() {
        stubFor(post(urlMatching("/juridisklogg.*"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));
    }

    private void stubPutAdministrerforsendelseOppdatertForsendelsestatusAndkonvId() {
        stubFor(put(urlMatching("/administrerforsendelse\\?forsendelseId=" + FORSENDELSE_ID + "\\&forsendelseStatus=OVERSENDT\\&konversasjonsId=.*"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));
    }

    private void stubGetEregHentOrgNavn(String orgnr) {
        stubFor(get("/ereg/v1/organisasjon/" + orgnr + "/noekkelinfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .withBodyFile("ereg/eregHentNavn_happy.json")));
    }

    public static void stubGetServiceRegistry() {
        stubFor(get(urlMatching("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + EformidlingConstants.AVTALTMELDING_PROCESS))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(classpathToString("__files/serviceregistry/serviceregistry_happy_response.json"))));
    }

    private void stubGetSecurityToken() {
        stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse().withStatus(HttpStatus.OK
                .value())
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .withBodyFile("securitytoken/stsResponse_happy.json")));
    }

    private void stubPostMaskinporten() {

        stubFor(post(urlMatching("/maskinporten"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(classpathToString("__files/maskinporten/maskinporten_happy_response.json"))));

    }

    private void stubGetForsendelse(String responsebody) {
        stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(classpathToString(responsebody).replace("insertCallIdHere", CALL_ID))));
    }

    private void stubPostSafJournalpost(String stringInRequestBody, String returnBodyFileName) {
        stubFor(post(urlMatching("/safgraphql"))
                .withRequestBody(containing(stringInRequestBody))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBodyFile(returnBodyFileName)));
    }

    private void sendStringMessage(javax.jms.Queue queue, final String message) {
        sendStringMessage(queue, message, CALL_ID);
    }


    private String findConversationId() {
        List<LoggedRequest> loggedRequests = findAll(postRequestedFor(urlEqualTo("/brokerserviceexternal")));
        String requestStr = loggedRequests.get(0).getBodyAsString();
        try {
            MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            SOAPMessage message = factory.createMessage(null,
                    new ByteArrayInputStream(requestStr.getBytes()));
            Unmarshaller unmarshaller = JAXBContext.newInstance(InitiateBrokerService.class).createUnmarshaller();
            Manifest uploadManifest = ((InitiateBrokerService) unmarshaller.unmarshal(message.getSOAPBody().extractContentAsDocument())).getBrokerServiceInitiation().getManifest();

            return uploadManifest.getSendersReference();
        } catch (Exception e) {
            fail("Fant ikke konversasjonsId. Feil: " + e.getMessage(), e.getCause());
            return null;
        }
    }

    private void sendStringMessage(javax.jms.Queue queue, final String message, final String callId) {
        jmsTemplate.send(queue, session -> {
            TextMessage msg = new ActiveMQTextMessage();
            msg.setText(message);
            if (callId != null) {
                msg.setStringProperty("callId", callId);
            }
            return msg;
        });
    }
}
