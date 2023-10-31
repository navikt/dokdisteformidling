package no.nav.dokdisteformidling.consumer.juridisklogg;

import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponse;
import org.springframework.stereotype.Component;

import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;

@Component
public class LagreJuridiskLoggMapper {

	private static final Integer ANTALL_AAR_LAGRES = 10;

	public LoggMeldingRequest map(HentForsendelseResponse hentForsendelseResponse, byte[] meldingsInnhold) {
		return LoggMeldingRequest.builder()
				.meldingsId(hentForsendelseResponse.getBestillingsId())
				.avsender(APP_NAME)
				.mottaker(hentForsendelseResponse.getMottaker().getMottakerId())
				.joarkRef(hentForsendelseResponse.getArkivInformasjon().getArkivId())
				.meldingsInnhold(meldingsInnhold)
				.antallAarLagres(ANTALL_AAR_LAGRES)
				.build();
	}
}
