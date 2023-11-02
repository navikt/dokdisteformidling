package no.nav.dokdisteformidling.consumer.juridisklogg;

public interface JuridiskLogg {

	LoggMeldingResponse lagreJuridiskLogg(final LoggMeldingRequest loggMeldingRequest);
}