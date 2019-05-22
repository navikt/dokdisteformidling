package no.nav.dokdisteformidling.qdist011;

import no.nav.dokdisteformidling.storage.DokdistDokument;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Component
public class LastOppDokumentRoute extends SpringRouteBuilder {

	public static final String ROUTE_ID = "LASTOPPDOKUMENT";
	public static final String ROUTE = "direct:" + ROUTE_ID;

	private static final String PROPERTY_FILNAVN = "filnavn";
	private static final String SFTP_FILETYPE = ".pdf";
	private static final String SFTP_FILE_CONFIG = "binary=true&fileName=${exchangeProperty." + PROPERTY_FILNAVN + "}" + SFTP_FILETYPE + "&";
	private static final String SFTP_SECURITY_CONFIG = "privateKeyFile={{sftp.privateKeyFile}}&privateKeyPassphrase={{sftp.privateKeyPassphrase}}&preferredAuthentications=publickey";
	private static final String SFTP_SERVER = "sftp://{{sftp.url}}:{{sftp.port}}/{{sftp.remoteFilePath}}?username={{sftp.username}}&***passord=gammelt_passord***;

	@Override
	public void configure() {
		from(ROUTE)
				.routeId(ROUTE_ID)
				.setExchangePattern(ExchangePattern.InOnly)
				.setProperty(PROPERTY_FILNAVN, bodyAs(DokdistDokument.class).method("getDokumentObjektReferanse"))
				.setBody(bodyAs(DokdistDokument.class).method("getPdf"))
				.to(SFTP_SERVER)
				.log(LoggingLevel.INFO, log, "qdist011 har lastet opp ${exchangeProperty." + PROPERTY_FILNAVN + "}.pdf til NFS fileshare for distribusjon via DPI");
	}
}
