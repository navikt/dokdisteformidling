package no.nav.dokdisteformidling.sdist001.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants;
import no.nav.dokdisteformidling.sdist001.Sdist001Service;
import no.nav.dokdisteformidling.sdist001.itest.config.ApplicationTestConfig;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static no.nav.dokdisteformidling.testUtils.classpathToByteArray;
import static no.nav.dokdisteformidling.testUtils.classpathToString;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.TRANSFER_ENCODING;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class},
        webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
class Sdist001IT {

    private static final Integer FORSENDELSE_ID = 1;
    public static final String SCENARIO_BROKERSERVICEEXTERNAL = "brokerserviceexternal";
    public static final String SCENARIO_STATE_GET_AVAILABLE_FILES_DONE = "GetAvailableFilesDone";

    @Autowired
    private Sdist001Service sdist001Service;

    @BeforeEach
    public void setupBefore() {
        WireMock.reset();
        WireMock.resetAllRequests();
        WireMock.removeAllMappings();

        stubAzure();
    }

    @Test
    void shouldOppdatereTilEkspedert() throws IOException {
        stubPostMaskinporten();
        stubGetServiceRegistry();
        stubGetHentEformidlingForsendelserBEKREFTETStatus();
        stubPostBrokerserviceExternalGetAvailableFiles();
        stubPostBrokerServiceExternalStreamedDownloadFileStreamed();
        stubGetAdministrerforsendleseHentForsendelse();
        stubPostLagreJuridiskLogg();
        stubPutAdministrerForsendelseOppdaterForsendelseTilEKSPEDERT();
        stubPostBrokerserviceExternalConfirmDownloaded();

        sdist001Service.oppdatereDokDistEformidlingStatus();
        verify(1, postRequestedFor(urlEqualTo("/maskinporten")));
        verify(1, getRequestedFor(urlEqualTo("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + EformidlingConstants.AVTALTMELDING_PROCESS)));
        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteformidlingforsendelser?distribusjonKanal=TRYGDERETTEN")));
        verify(2, postRequestedFor(urlEqualTo("/brokerserviceexternal")));
        verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternalstreamed")));
        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/1")));
        verify(1, postRequestedFor(urlEqualTo("/juridiskLogg")));
        verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=1&forsendelseStatus=EKSPEDERT")));
    }

    void stubAzure() {
        stubFor(post("/azure_token")
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("azure/token_response.json")));
    }

    private void stubPostMaskinporten() {
        stubFor(post(urlMatching("/maskinporten"))
                .willReturn(aResponse().withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("maskinporten/maskinporten_happy_response.json")));
    }

    private void stubGetServiceRegistry() {
        stubFor(get(urlMatching("/serviceregistry/identifier/" + EformidlingConstants.TRYGDERETTEN_ORGNUMMER + "/process/" + EformidlingConstants.AVTALTMELDING_PROCESS))
                .willReturn(aResponse().withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("serviceregistry/serviceregistry_happy_response.json")));
    }

    private void stubGetHentEformidlingForsendelserBEKREFTETStatus() {
        stubFor(get("/administrerforsendelse/henteformidlingforsendelser?distribusjonKanal=TRYGDERETTEN")
                .willReturn(aResponse().withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("rdist001/henteformidlingforsendelser-bekreftetStatus.json")));
    }

    private void stubPostBrokerserviceExternalGetAvailableFiles() {
        stubFor(post(urlEqualTo("/brokerserviceexternal"))
                .inScenario(SCENARIO_BROKERSERVICEEXTERNAL)
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo(SCENARIO_STATE_GET_AVAILABLE_FILES_DONE)
                .willReturn(aResponse().withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)
                        .withBodyFile("altinn/brokerserviceexternal/getavailablefiles_happy_response.xml")));
    }

    private void stubPostBrokerServiceExternalStreamedDownloadFileStreamed() throws IOException {
        String boundary = "uuid:c678c2f3-c620-4d19-9884-fc1c36c1d29a+id=174513";

        stubFor(post(urlMatching("/brokerserviceexternalstreamed"))
                .willReturn(aResponse().withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, String.format("multipart/related; type=\"application/xop+xml\"; start=\"<http://tempuri.org/1>\"; boundary=\"%s\"; start-info=\"text/xml\"", boundary))
                        .withHeader(TRANSFER_ENCODING, "chunked")
                        .withHeader("MIME-Version", "1.0")
                        .withBody(getDownloadBody(boundary))));
    }

    private byte[] getDownloadBody(String boundary) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final Charset utf8 = StandardCharsets.UTF_8;
        IOUtils.write("--" + boundary + "\r\n", bos, utf8);
        IOUtils.write("Content-ID: <http://tempuri.org/1>\r\n", bos, utf8);
        IOUtils.write("Content-Transfer-Encoding: 8bit\r\n", bos, utf8);
        IOUtils.write("Content-Type: application/xop+xml; charset=UTF-8; type=\"text/xml\"\r\n", bos, utf8);
        IOUtils.write("\r\n", bos, utf8);
        IOUtils.write(classpathToString("__files/altinn/brokerserviceexternalstreamed/downloadfilestreamed_happy_response.xml"), bos, utf8);
        IOUtils.write("\r\n", bos, utf8);
        IOUtils.write("--" + boundary + "\r\n", bos, utf8);
        IOUtils.write("Content-ID: <http://tempuri.org/1/637169441367559832>\r\n", bos, utf8);
        IOUtils.write("Content-Transfer-Encoding: binary\r\n", bos, utf8);
        IOUtils.write("Content-Type: application/octet-stream\r\n", bos, utf8);
        IOUtils.write("\r\n", bos, utf8);
        IOUtils.write(classpathToByteArray("__files/zip/altinn_sbd_kvittering_LEST.zip"), bos);
        IOUtils.write("\r\n", bos, utf8);
        IOUtils.write("--" + boundary, bos, utf8);
        return bos.toByteArray();
    }

    private void stubGetAdministrerforsendleseHentForsendelse() {
        stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBodyFile("rdist001/getForsendelse-BEKREFTET.json")));
    }

    private void stubPostLagreJuridiskLogg() {
        stubFor(post(urlMatching("/juridiskLogg")).willReturn(aResponse().withStatus(OK.value())));
    }

    private void stubPutAdministrerForsendelseOppdaterForsendelseTilEKSPEDERT() {
        stubFor(put("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=EKSPEDERT")
                .willReturn(aResponse().withStatus(OK.value())));
    }

    private void stubPostBrokerserviceExternalConfirmDownloaded() {
        stubFor(post(urlEqualTo("/brokerserviceexternal"))
                .inScenario(SCENARIO_BROKERSERVICEEXTERNAL)
                .whenScenarioStateIs(SCENARIO_STATE_GET_AVAILABLE_FILES_DONE)
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)
                        .withBodyFile("altinn/brokerserviceexternal/confirmdownloaded_happy_response.xml")));
    }
}
