package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.AVTALEMELDING_PROCESS;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.ServiceIdentifier.DPO;

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
		final IdentifierResource identifierResource = serviceRegistryConsumer.getIdentifierResource(TRYGDERETTEN_ORGNUMMER, AVTALEMELDING_PROCESS);
		final Optional<ServiceRecord> anyServiceRecord = identifierResource.findServiceRecord(AVTALEMELDING_PROCESS, DPO);
		final ServiceRecord serviceRecord = anyServiceRecord
				.orElseThrow(() -> new MottakerInfoIkkeFunnetException("Fant ikke mottakerinfo for organisasjon=" + TRYGDERETTEN_ORGNUMMER + " og prosess=" + AVTALEMELDING_PROCESS));
		final Service service = serviceRecord.getService();
		return new MottakerInfo(serviceRecord.getOrganisationNumber(),
				serviceRecord.getPemCertificate(),
				service.getServiceCode(),
				service.getServiceEditionCode());
	}
}
