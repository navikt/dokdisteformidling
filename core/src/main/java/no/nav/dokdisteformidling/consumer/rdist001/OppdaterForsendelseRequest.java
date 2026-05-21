package no.nav.dokdisteformidling.consumer.rdist001;

public record OppdaterForsendelseRequest(
		Long forsendelseId,
		String forsendelseStatus,
		String konversasjonId,
		byte[] forsendelseMetadata,
		String forsendelseMetadataType) {

	public OppdaterForsendelseRequest {
		boolean harForsendelseMetadata = forsendelseMetadata != null;
		boolean harForsendelseMetadataType = forsendelseMetadataType != null;
		if (harForsendelseMetadata != harForsendelseMetadataType) {
			throw new IllegalArgumentException("forsendelseMetadata og forsendelseMetadataType må enten begge være satt eller begge være null");
		}
	}
}
