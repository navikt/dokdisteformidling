package no.nav.dokdisteformidling.sdist001;

import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_BEKREFTET;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_EKSPEDERT;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_FEILET;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_OVERSENDT;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentEformidlingforsendelserResponseTo;
import no.nav.dokdisteformidling.exception.functional.Rdist001OppdaterForsendelseStatusFunctionalException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Erik Bråten, Visma Consulting.
 */
public class Sdist001ScheduledTest {

	private final String DISTRIBUSJON_KANAL = "TRYGDERETTEN";

	private final AdministrerForsendelse administrerForsendelse = mock(AdministrerForsendelse.class);
	private final Sdist001Scheduled sdist001Scheduled = new Sdist001Scheduled(administrerForsendelse);

	@AfterEach
	public void cleanUp() {
		reset(administrerForsendelse);
	}

	@Test
	public void shouldHenteTomListeOk() {
		when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(responseWithNoForsendelser());

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(administrerForsendelse).hentEformidlingForsendelser();
	}

	@Test
	public void shouldNotContactIntegrationPointWhenIllegalForsendelseStatus() {
		when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(responseWithIllegalForsendelseStatus());

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(administrerForsendelse).hentEformidlingForsendelser();
		//verify(integrationPoint, never()).contact();
	}

	//@Test
	public void shouldSetForsendelseStatusOversendtToFeilet() {
		when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(responseWithForsendelseStatusOversendt());
		// mock integrasjonspunkt respons KONVERSASJON_STATUS_TTL_EXPIRED

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(administrerForsendelse).hentEformidlingForsendelser();
		// verify integrasjonspunkt ble kalt
		verify(administrerForsendelse).oppdaterForsendelseStatus("1", FORSENDELSE_STATUS_FEILET);
	}

	//@Test
	public void shouldSetForsendelseStatusOversendtToBekreftet() {
		when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(responseWithForsendelseStatusOversendt());
		// mock integrasjonspunkt respons KONVERSASJON_STATUS_SENDT

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(administrerForsendelse).hentEformidlingForsendelser();
		// verify integrasjonspunkt ble kalt
		verify(administrerForsendelse).oppdaterForsendelseStatus("1", FORSENDELSE_STATUS_BEKREFTET);
	}

	//@Test
	public void shouldSetForsendelseStatusBekreftetToFeilet() {
		when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(responseWithForsendelseStatusBekreftet(1));
		// mock integrasjonspunkt respons KONVERSASJON_STATUS_TTL_EXPIRED

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(administrerForsendelse).hentEformidlingForsendelser();
		// verify integrasjonspunkt ble kalt
		verify(administrerForsendelse).oppdaterForsendelseStatus("1", FORSENDELSE_STATUS_FEILET);
	}

	//@Test
	public void shouldSetForsendelseStatusBekreftetToEkspedert() {
		when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(responseWithForsendelseStatusBekreftet(3));
		// mock integrasjonspunkt respons KONVERSASJON_STATUS_MOTTATT
		// mock integrasjonspunkt respons KONVERSASJON_STATUS_LEVERT
		// mock integrasjonspunkt respons KONVERSASJON_STATUS_LEST
		// mock juridisklogg

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(administrerForsendelse).hentEformidlingForsendelser();
		// verify integrasjonspunkt ble kalt for KONVERSASJON_STATUS_MOTTATT
		// verify integrasjonspunkt ble kalt for KONVERSASJON_STATUS_LEVERT
		// verify integrasjonspunkt ble kalt for KONVERSASJON_STATUS_LEST
		verify(administrerForsendelse).oppdaterForsendelseStatus("1", FORSENDELSE_STATUS_EKSPEDERT);
		verify(administrerForsendelse).oppdaterForsendelseStatus("2", FORSENDELSE_STATUS_EKSPEDERT);
		verify(administrerForsendelse).oppdaterForsendelseStatus("3", FORSENDELSE_STATUS_EKSPEDERT);
		// verify juridisklogg ble kalt
	}

	//@Test
	public void shouldProcessAllForsendelserWhenFunctionalException() {
		when(administrerForsendelse.hentEformidlingForsendelser()).thenReturn(responseWithForsendelseStatusBekreftet(2));
		// mock integrasjonspunkt respons KONVERSASJON_STATUS_TTL_EXPIRED
		doThrow(new Rdist001OppdaterForsendelseStatusFunctionalException("Exception", new Exception()))
				.when(administrerForsendelse).oppdaterForsendelseStatus("1", FORSENDELSE_STATUS_FEILET);
		// mock integrasjonspunkt respons KONVERSASJON_STATUS_MOTTATT
		// mock juridisklogg

		sdist001Scheduled.oppdaterEformidlingStatus();

		verify(administrerForsendelse).hentEformidlingForsendelser();
		// verify integrasjonspunkt ble kalt for KONVERSASJON_STATUS_TTL_EXPIRED
		verify(administrerForsendelse).oppdaterForsendelseStatus("1", FORSENDELSE_STATUS_FEILET);
		// verify integrasjonspunkt ble kalt for KONVERSASJON_STATUS_LEVERT
		verify(administrerForsendelse).oppdaterForsendelseStatus("2", FORSENDELSE_STATUS_EKSPEDERT);
		// verify juridisklogg ble kalt
	}

	private HentEformidlingforsendelserResponseTo responseWithNoForsendelser() {
		List<HentEformidlingforsendelserResponseTo.ForsendelseTo> forsendelser = new ArrayList<HentEformidlingforsendelserResponseTo.ForsendelseTo>();
		return HentEformidlingforsendelserResponseTo.builder().forsendelser(forsendelser).build();
	}

	private HentEformidlingforsendelserResponseTo responseWithIllegalForsendelseStatus() {
		HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelse;
		List<HentEformidlingforsendelserResponseTo.ForsendelseTo> forsendelser = new ArrayList<HentEformidlingforsendelserResponseTo.ForsendelseTo>();
		forsendelse = HentEformidlingforsendelserResponseTo.ForsendelseTo.builder()
				.forsendelseId(1L).forsendelseStatus(FORSENDELSE_STATUS_EKSPEDERT).distribusjonKanal(DISTRIBUSJON_KANAL).konversasjonId("101")
				.build();
		forsendelser.add(forsendelse);
		return HentEformidlingforsendelserResponseTo.builder().forsendelser(forsendelser).build();
	}

	private HentEformidlingforsendelserResponseTo responseWithForsendelseStatusOversendt() {
		HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelse;
		List<HentEformidlingforsendelserResponseTo.ForsendelseTo> forsendelser = new ArrayList<HentEformidlingforsendelserResponseTo.ForsendelseTo>();
		forsendelse = HentEformidlingforsendelserResponseTo.ForsendelseTo.builder()
				.forsendelseId(1L).forsendelseStatus(FORSENDELSE_STATUS_OVERSENDT).distribusjonKanal(DISTRIBUSJON_KANAL).konversasjonId("101")
				.build();
		forsendelser.add(forsendelse);
		return HentEformidlingforsendelserResponseTo.builder().forsendelser(forsendelser).build();
	}

	private HentEformidlingforsendelserResponseTo responseWithForsendelseStatusBekreftet(int count) {
		HentEformidlingforsendelserResponseTo.ForsendelseTo forsendelse;
		List<HentEformidlingforsendelserResponseTo.ForsendelseTo> forsendelser = new ArrayList<HentEformidlingforsendelserResponseTo.ForsendelseTo>();
		for (int i=1; i <= count; i++) {
			forsendelse = HentEformidlingforsendelserResponseTo.ForsendelseTo.builder()
					.forsendelseId((long) i).forsendelseStatus(FORSENDELSE_STATUS_BEKREFTET).distribusjonKanal(DISTRIBUSJON_KANAL).konversasjonId(i + 100 + "")
					.build();
			forsendelser.add(forsendelse);
		}
		return HentEformidlingforsendelserResponseTo.builder().forsendelser(forsendelser).build();
	}

}
