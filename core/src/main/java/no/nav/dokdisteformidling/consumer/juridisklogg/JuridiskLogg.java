package no.nav.dokdisteformidling.consumer.juridisklogg;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public interface JuridiskLogg {

	LoggMeldingResponse lagreJuridiskLogg(final LoggMeldingRequest loggMeldingRequest);
}