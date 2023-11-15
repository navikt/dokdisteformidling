package no.nav.dokdisteformidling.config.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

public class Http11OnlyFeature extends AbstractFeature {

	/**
	 * en bug i wiremock gjør at streams og http2 ikke funker - derfor må vi tvinge frem kun http 1.1 i tester
	 */
	public Http11OnlyFeature() {
	}

	@Override
	public void initialize(Client client, Bus bus) {
		Conduit conduit = client.getConduit();

		if (conduit instanceof HTTPConduit httpConduit) {
			if (httpConduit.getClient() == null) {
				HTTPClientPolicy policy = new HTTPClientPolicy();
				policy.setVersion("1.1");
				httpConduit.setClient(policy);
			} else {
				httpConduit.getClient().setVersion("1.1");
			}
		}

		super.initialize(client, bus);
	}
}
