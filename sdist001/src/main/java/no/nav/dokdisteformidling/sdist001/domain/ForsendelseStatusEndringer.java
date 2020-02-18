package no.nav.dokdisteformidling.sdist001.domain;

import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Value
@NoArgsConstructor
public class ForsendelseStatusEndringer {
	Set<String> bekreftet = new HashSet<>();
	Set<String> ekspedert = new HashSet<>();
	Set<String> feilet = new HashSet<>();

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(getBekreftet().size());
		sb.append(" forsendelser satt til bekreftet");
		if (getBekreftet().size() > 0) {
			sb.append(": " + StringUtils.collectionToCommaDelimitedString(getBekreftet()));
		}
		sb.append(". ");

		sb.append(getEkspedert().size());
		sb.append(" forsendelser satt til ekspedert");
		if (getEkspedert().size() > 0) {
			sb.append(": " + StringUtils.collectionToCommaDelimitedString(getEkspedert()));
		}
		sb.append(". ");

		sb.append(getFeilet().size());
		sb.append(" forsendelser satt til feilet");
		if (getFeilet().size() > 0) {
			sb.append(": " + StringUtils.collectionToCommaDelimitedString(getFeilet()));
		}
		sb.append(".");

		return sb.toString();
	}

	public void clear() {
		getBekreftet().clear();
		getFeilet().clear();
		getEkspedert().clear();
	}
}
