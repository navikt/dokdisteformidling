package no.nav.dokdisteformidling.consumer.rdist001;

public record OppdaterForsendelseRequest(
		Long forsendelseId,
		String forsendelseStatus,
		String konversasjonId) {
}
