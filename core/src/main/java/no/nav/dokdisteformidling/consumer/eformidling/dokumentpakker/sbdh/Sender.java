package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh;

public class Sender extends Partner {

	@Override
	public Sender setIdentifier(PartnerIdentification identifier) {
		this.identifier = identifier;
		return this;
	}
}
