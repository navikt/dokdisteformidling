package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.ARKIVMELDING_PROCESS;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;

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
