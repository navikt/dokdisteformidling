package no.nav.dokdisteformidling.consumer.rdist001;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public interface AdministrerForsendelse {

	HentForsendelseResponseTo hentForsendelse(final String forsendelseId);

	void oppdaterForsendelseStatus(final String forsendelseId, final String forsendelseStatus);

	void oppdaterForsendelseStatusOgKonversasjonsId(final String forsendelseId, final String forsendelseStatus, final String konversasjonsId);

	HentEformidlingforsendelserResponseTo hentEformidlingForsendelser();
}
