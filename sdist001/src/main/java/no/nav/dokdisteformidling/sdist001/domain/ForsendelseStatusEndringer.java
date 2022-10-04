package no.nav.dokdisteformidling.sdist001.domain;

import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;

@Value
@NoArgsConstructor
public class ForsendelseStatusEndringer {
	Set<String> bekreftet = new HashSet<>();
	Set<String> ekspedert = new HashSet<>();
	Set<String> feilet = new HashSet<>();

	@Override
	public String toString() {
		return """
				antall_bekreftet=%d, bekreftet=%s. antall_ekspedert=%d, ekspedert=%s. antall_feilet=%d, feilet=%s."""
				.formatted(getBekreftet().size(), bekreftet, getEkspedert().size(), getEkspedert(), getFeilet().size(), getFeilet());
	}
}
