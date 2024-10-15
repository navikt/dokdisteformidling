package no.nav.dokdisteformidling.qdist013.itest;

import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EformidlingMottakerInfoServiceIT extends AbstractQdist013IntegrationTest {

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
