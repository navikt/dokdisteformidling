package no.nav.dokdisteformidling.constants;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public final class DomainConstants {
	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("Europe/Oslo");
	public static final ZoneId DEFAULT_ZONE_ID = DEFAULT_TIME_ZONE.toZoneId();

	public static final String APP_NAME = "dokdisteformidling";
	public static final String FORSENDELSE_STATUS_KLAR_FOR_DIST = "KLAR_FOR_DIST";
	public static final String FORSENDELSE_STATUS_OVERSENDT = "OVERSENDT";
	public static final String DISTRIBUSJONSKANAL = "TRYGDERETTEN";

	public static final String BEARER_PREFIX = "Bearer ";

	public static final String VARIANTFORMAT_SLADDET = "SLADDET";
	public static final String VARIANTFORMAT_ARKIV = "ARKIV";
	public static final String VARIANTFORMAT_PRODUKSJON = "PRODUKSJON";

	private DomainConstants() {
	}
}
