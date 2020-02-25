package no.nav.dokdisteformidling.sdist001.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants;
import no.nav.dokdisteformidling.sdist001.Sdist001Service;
import no.nav.dokdisteformidling.sdist001.itest.config.ApplicationTestConfig;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

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
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

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

    private static final Integer FORSENDELSE_ID = 1;

    @Inject
    private Sdist001Service sdist001Service;

    @BeforeEach
    public void setupBefore() {
        WireMock.reset();
        WireMock.resetAllRequests();
        WireMock.removeAllMappings();
    }

    @Test
    public void shouldOppdatereTilEkspedert() throws IOException {
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
        verify(1, getRequestedFor(urlEqualTo("/serviceregistry/identifier/" + TRYGDERETTEN_ORGNUMMER + "/process/" + EformidlingConstants.ARKIVMELDING_PROCESS)));
        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteformidlingforsendelser")));
        verify(2, postRequestedFor(urlEqualTo("/brokerserviceexternal")));
        verify(1, postRequestedFor(urlEqualTo("/brokerserviceexternalstreamed")));
        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/1")));
        verify(1, postRequestedFor(urlEqualTo("/juridiskLogg")));
        verify(1, putRequestedFor(urlEqualTo("/administrerforsendelse?forsendelseId=1&forsendelseStatus=EKSPEDERT")));
    }


    private void stubPostMaskinporten() {
        stubFor(post(urlMatching("/maskinporten"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(classpathToString("__files/maskinporten/maskinporten_happy_response.json"))));
    }

    private void stubGetServiceRegistry() {
        stubFor(get(urlMatching("/serviceregistry/identifier/" + EformidlingConstants.TRYGDERETTEN_ORGNUMMER + "/process/" + EformidlingConstants.ARKIVMELDING_PROCESS))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(classpathToString("__files/serviceregistry/serviceregistry_happy_response.json"))));
    }

    private void stubGetHentEformidlingForsendelserBEKREFTETStatus() {
        stubFor(get("/administrerforsendelse/henteformidlingforsendelser")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(classpathToString("__files/rdist001/henteformidlingforsendelser-bekreftetStatus.json"))));
    }

    private void stubPostBrokerserviceExternalGetAvailableFiles() {
        stubFor(post(urlEqualTo("/brokerserviceexternal"))
                .inScenario("brokerserviceexternal")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                        .withBody(classpathToString("__files/altinn/brokerserviceexternal/getavailablefiles_happy_response.xml")))
                .willSetStateTo("GetAvailableFilesDone"));
    }

    private void stubPostBrokerServiceExternalStreamedDownloadFileStreamed() throws IOException {
        String boundary = "uuid:c678c2f3-c620-4d19-9884-fc1c36c1d29a+id=174513";

        stubFor(post(urlMatching("/brokerserviceexternalstreamed"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, String.format("multipart/related; type=\"application/xop+xml\";start=\"<http://tempuri.org/0>\";boundary=\"%s\";start-info=\"text/xml\"", boundary))
                        .withHeader(HttpHeaders.TRANSFER_ENCODING, "chunked")
                        .withHeader("MIME-Version", "1.0")
                        .withBody(getDownloadBody(boundary))));
    }

    private byte[] getDownloadBody(String boundary) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (PrintWriter pw = new PrintWriter(bos)) {
            pw.println("--" + boundary);
            pw.println("Content-ID: <http://tempuri.org/0>");
            pw.println("Content-Transfer-Encoding: 8bit");
            pw.println("Content-Type: application/xop+xml;charset=utf-8;type=\"text/xml\"");
            pw.println();
            pw.println("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><DownloadFileStreamedResponse xmlns=\"http://www.altinn.no/services/ServiceEngine/Broker/2015/06\"><DownloadFileStreamedResult><xop:Include href=\"cid:http://tempuri.org/1/***gammelt_fnr***7559832\" xmlns:xop=\"http://www.w3.org/2004/08/xop/include\"/></DownloadFileStreamedResult></DownloadFileStreamedResponse></s:Body></s:Envelope>");
            pw.println();
            pw.println("--" + boundary);
            pw.println("Content-ID: <http://tempuri.org/1/***gammelt_fnr***7559832>");
            pw.println("Content-Transfer-Encoding: binary");
            pw.println("Content-Type: application/octet-stream");
            pw.println();
            pw.flush();
            bos.write(classpathToByteArray("__files/zip/altinn_sbd_kvittering_LEST.zip"));
            bos.flush();
            pw.println();
            pw.println("--" + boundary);
            pw.flush();
        }

        return bos.toByteArray();

    }

    private void stubGetAdministrerforsendleseHentForsendelse() {
        stubFor(get("/administrerforsendelse/" + FORSENDELSE_ID).willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(classpathToString("__files/rdist001/getForsendelse-BEKREFTET.json"))));
    }

    private void stubPostLagreJuridiskLogg() {
        stubFor(post(urlMatching("/juridiskLogg")).willReturn(aResponse().withStatus(HttpStatus.OK.value())));
    }

    private void stubPutAdministrerForsendelseOppdaterForsendelseTilEKSPEDERT() {
        stubFor(put("/administrerforsendelse?forsendelseId=" + FORSENDELSE_ID + "&forsendelseStatus=EKSPEDERT")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));
    }

    private void stubPostBrokerserviceExternalConfirmDownloaded() {
        stubFor(post(urlEqualTo("/brokerserviceexternal"))
                .inScenario("brokerserviceexternal")
                .whenScenarioStateIs("GetAvailableFilesDone")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                        .withBody(classpathToString("__files/altinn/brokerserviceexternal/confirmdownloaded_happy_response.xml"))));
    }
}
