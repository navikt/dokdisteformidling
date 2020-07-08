package no.nav.dokdisteformidling.consumer.juridisklogg;

import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import org.springframework.stereotype.Component;

import static no.nav.dokdisteformidling.constants.DomainConstants.APP_NAME;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class LagreJuridiskLoggMapper {

	private static final Integer ANTALL_AAR_LAGRES = 10;

	public LoggMeldingRequest map(HentForsendelseResponseTo hentForsendelseResponseTo, byte[] meldingsInnhold) {
		return LoggMeldingRequest.builder()
				.meldingsId(hentForsendelseResponseTo.getBestillingsId())
				.avsender(APP_NAME)
				.mottaker(hentForsendelseResponseTo.getMottaker().getMottakerId())
				.joarkRef(hentForsendelseResponseTo.getArkivInformasjon().getArkivId())
				.meldingsInnhold(meldingsInnhold)
				.antallAarLagres(ANTALL_AAR_LAGRES)
				.build();
	}
}
