package no.nav.dokdisteformidling.consumer.rdist001;

public interface AdministrerForsendelse {

	HentForsendelseResponse hentForsendelse(final Long forsendelseId);

	void oppdaterForsendelse(OppdaterForsendelseRequest oppdaterForsendelseRequest);

	HentEformidlingforsendelserResponse hentEformidlingForsendelser();
}
