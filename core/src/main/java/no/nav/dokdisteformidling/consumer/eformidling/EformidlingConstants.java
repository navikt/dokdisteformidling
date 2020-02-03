package no.nav.dokdisteformidling.consumer.eformidling;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class EformidlingConstants {
	public static final String NAV_ORGNUMMER = "889640782"; // hardkodet inntil videre fordi DPO-bruker er knyttet til denne (MMA-3834)
	// TODO teknisk verifisering. Trygderetten sitt orgnummer er 974761084. Hvis orgnummer er noe annet så er det en feil.
	// TODO difi test org 987464291
	public static final String TRYGDERETTEN_ORGNUMMER = "889640782";
	public static final String ARKIVMELDING_PROCESS = "urn:no:difi:profile:arkivmelding:administrasjon:ver1.0";

	private EformidlingConstants() {

	}
}
