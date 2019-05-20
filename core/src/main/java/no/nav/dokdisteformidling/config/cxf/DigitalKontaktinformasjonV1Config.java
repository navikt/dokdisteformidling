package no.nav.dokdisteformidling.config.cxf;

import no.nav.dokdisteformidling.config.alias.DigitalKontaktinformasjonV1Alias;
import no.nav.dokdisteformidling.config.sts.STSConfig;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.binding.DigitalKontaktinformasjonV1;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import javax.xml.namespace.QName;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Configuration
public class DigitalKontaktinformasjonV1Config extends AbstractCxfEndpointConfig {

	private static final String ROOT_PATH = "wsdl/no/nav/tjeneste/virksomhet/";
	public static final String DKI_WSDL = ROOT_PATH + "digitalKontaktinformasjon/v1/Binding.wsdl";

	public static final String BINDING_NAMESPACE_URI = "http://nav.no/tjeneste/virksomhet/digitalKontaktinformasjon/v1/Binding";
	public static final QName SERVICE_QNAME = new QName(BINDING_NAMESPACE_URI, "DigitalKontaktinformasjon_v1");
	public static final QName PORT_QNAME = new QName(BINDING_NAMESPACE_URI, "DigitalKontaktinformasjon_v1Port");

	@Inject
	public DigitalKontaktinformasjonV1Config(Bus bus) {
		super(bus);
	}

	@Bean
	public DigitalKontaktinformasjonV1 digitalKontaktinformasjonPort(DigitalKontaktinformasjonV1Alias digitalKontaktinformasjonV1Alias, STSConfig stsConfig) {
		setWsdlUrl(DKI_WSDL);
		setServiceName(SERVICE_QNAME);
		setEndpointName(PORT_QNAME);
		setAddress(digitalKontaktinformasjonV1Alias.getEndpointurl());
		setReceiveTimeout(digitalKontaktinformasjonV1Alias.getReadtimeoutms());
		setConnectTimeout(digitalKontaktinformasjonV1Alias.getConnecttimeoutms());
		addFeature(new WSAddressingFeature());

		DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1 = createPort(DigitalKontaktinformasjonV1.class);
		stsConfig.configureSTS(digitalKontaktinformasjonV1);

		return digitalKontaktinformasjonV1;
	}
}
