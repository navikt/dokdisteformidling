package no.nav.dokdisteformidling.qdist013.itest;

import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import no.nav.dokdisteformidling.qdist013.itest.config.ApplicationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static no.nav.dokdisteformidling.qdist013.itest.AbstractQdist013IntegrationTest.stubGetSecurityToken;
import static no.nav.dokdisteformidling.qdist013.itest.AbstractQdist013IntegrationTest.stubGetServiceRegistry;
import static no.nav.dokdisteformidling.qdist013.itest.AbstractQdist013IntegrationTest.stubPostMaskinporten;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@EnableAutoConfiguration
@SpringBootTest(classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class EformidlingMottakerInfoServiceIT {

	@Autowired
	private EformidlingMottakerInfoService eformidlingMottakerInfoService;

	@Test
	void shouldHenteMottakerInfoFraServiceRegistry() {
		stubGetSecurityToken();
		stubPostMaskinporten();
		stubGetServiceRegistry();

		MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();

		assertThat(mottakerInfo.getOrgnummer(), is(TRYGDERETTEN_ORGNUMMER));
		assertThat(mottakerInfo.getServiceCode(), is("4192"));
		assertThat(mottakerInfo.getServiceEditionCode(), is("270815"));
	}
}
