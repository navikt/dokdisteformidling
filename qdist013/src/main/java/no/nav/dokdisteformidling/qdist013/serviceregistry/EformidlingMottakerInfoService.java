package no.nav.dokdisteformidling.qdist013.serviceregistry;

import static no.nav.dokdisteformidling.qdist013.Qdist013Constants.ARKIVMELDING_PROCESS;
import static no.nav.dokdisteformidling.qdist013.Qdist013Constants.TRYGDERETTEN_ORGNUMMER;

import no.nav.dokdisteformidling.qdist013.Qdist013Constants;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class EformidlingMottakerInfoService {
	private final ServiceRegistryConsumer serviceRegistryConsumer;

	@Inject
	public EformidlingMottakerInfoService(ServiceRegistryConsumer serviceRegistryConsumer) {
		this.serviceRegistryConsumer = serviceRegistryConsumer;
	}

	public MottakerInfo hentMottakerInfoTrygderetten() {
		final IdentifierResource identifierResource = serviceRegistryConsumer.getIdentifierResource(TRYGDERETTEN_ORGNUMMER, ARKIVMELDING_PROCESS);
		final Optional<ServiceRecord> anyServiceRecord = identifierResource.findServiceRecord(ARKIVMELDING_PROCESS);
		final ServiceRecord serviceRecord = anyServiceRecord
				.orElseThrow(() -> new MottakerInfoIkkeFunnetException("Fant ikke mottakerinfo for organisation=" + TRYGDERETTEN_ORGNUMMER + " og process=" + ARKIVMELDING_PROCESS));
		final Service service = serviceRecord.getService();
		return new MottakerInfo(serviceRecord.getPemCertificate(),
				service.getServiceCode(),
				service.getServiceEditionCode());
	}
}
