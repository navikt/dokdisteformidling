package no.nav.dokdisteformidling.consumer.eformidling;

import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.UploadResponse;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface Eformidling {
	UploadResponse send(NavDokumentpakke navDokumentpakke) throws IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage;
}
