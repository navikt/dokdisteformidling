package no.nav.dokdisteformidling.qdist011.constants;

import no.difi.begrep.sdp.schema_v10.Iso6523Authority;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */

public final class BridgeMotSDPMapperConstants {

	public static final String VERSION = "1.0";
	public static final Iso6523Authority AUTHORITY_ENUM = Iso6523Authority.ISO_6523_ACTORID_UPIS;
	public static final String AUTHORITY = AUTHORITY_ENUM.value();
	public static final String STANDARD = "urn:no:difi:sdp:1.0";
	public static final String DIGITAL_POST = "digitalPost";
	public static final String BUSINESS_SCOPE_TYPE = "ConversationId";
	public static final String ORG_PREFIX = "9908:";
	public static final String ORGNR_NAV = ORG_PREFIX + "889640782";
	public static final String DOKUMENT_MIME = "application/pdf";
	public static final String EPOST = "EPOST";
	public static final String SMS = "SMS";
	public static final String RESERVASJON = "JA";
	public static final String SPRAAK_KODE = "no";
	public static final int DATE_VALID_MONTHS = 18;

	private BridgeMotSDPMapperConstants() {

	}
}
