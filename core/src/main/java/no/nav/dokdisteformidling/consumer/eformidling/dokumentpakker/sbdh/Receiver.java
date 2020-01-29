package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh;

public class Receiver extends Partner {

	@Override
	public Receiver setIdentifier(PartnerIdentification identifier) {
		this.identifier = identifier;
		return this;
	}
}
