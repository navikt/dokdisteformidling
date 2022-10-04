package no.nav.dokdisteformidling.sdist001.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForsendelseStatusEndringerTest {

	@Test
	void shouldReturnReportInToString() {
		ForsendelseStatusEndringer forsendelseStatusEndringer = createForsendelseStatusEndringer();

		assertThat(forsendelseStatusEndringer.toString())
				.isEqualTo("antall_bekreftet=2, bekreftet=[1, 2]. antall_ekspedert=1, ekspedert=[4]. antall_feilet=1, feilet=[3].");
	}

	@Test
	void shouldReturnReportInToStringWhenEmptyEntry() {
		ForsendelseStatusEndringer forsendelseStatusEndringer = createForsendelseStatusEndringer();
		forsendelseStatusEndringer.getFeilet().clear();

		assertThat(forsendelseStatusEndringer.toString())
				.isEqualTo("antall_bekreftet=2, bekreftet=[1, 2]. antall_ekspedert=1, ekspedert=[4]. antall_feilet=0, feilet=[].");
	}

	private ForsendelseStatusEndringer createForsendelseStatusEndringer() {
		ForsendelseStatusEndringer forsendelseStatusEndringer = new ForsendelseStatusEndringer();
		forsendelseStatusEndringer.getBekreftet().add("1");
		forsendelseStatusEndringer.getBekreftet().add("2");
		forsendelseStatusEndringer.getFeilet().add("3");
		forsendelseStatusEndringer.getEkspedert().add("4");
		return forsendelseStatusEndringer;
	}
}