/**
 * Copyright (C) Posten Norge AS
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.nav.dokdisteformidling.consumer.eformidling;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Organisasjonsnummer {
	static final Pattern ISO6523_PATTERN = Pattern.compile("^([0-9]{4}:)?([0-9]{9})?$");
	public static final String ISO6523_AUTHORITY = "iso6523-actorid-upis";
	public static final String ISO6523_PREFIX = "0192:";

	private Organisasjonsnummer() {
        //noop
	}

	public static String asIso6523(final String orgNummer) {
		return ISO6523_PREFIX + orgNummer;
	}

	public static String fromIso6523(final String iso6523Orgnr) {
		Matcher matcher = ISO6523_PATTERN.matcher(iso6523Orgnr);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid organization number. " +
					"Expected format is ISO 6523, got following organization number: " + iso6523Orgnr);
		}
		return matcher.group(2);
	}

	public static boolean isIso6523(final String iso6523Orgnr) {
		return ISO6523_PATTERN.matcher(iso6523Orgnr).matches();
	}
}
