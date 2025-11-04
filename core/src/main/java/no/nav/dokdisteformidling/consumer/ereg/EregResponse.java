package no.nav.dokdisteformidling.consumer.ereg;

public record EregResponse(Navn navn) {

	public record Navn(String sammensattnavn, String navnelinje1) {
	}

}