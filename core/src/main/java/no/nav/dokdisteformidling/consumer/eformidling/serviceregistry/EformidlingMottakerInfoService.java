package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import org.springframework.stereotype.Component;

import java.util.Optional;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.AVTALTMELDING_PROCESS;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.ServiceIdentifier.DPO;

@Component
public class EformidlingMottakerInfoService {
	private final ServiceRegistryConsumer serviceRegistryConsumer;

	public EformidlingMottakerInfoService(ServiceRegistryConsumer serviceRegistryConsumer) {
		this.serviceRegistryConsumer = serviceRegistryConsumer;
	}

	public MottakerInfo hentMottakerInfoTrygderetten() {
		final IdentifierResource identifierResource = serviceRegistryConsumer.getIdentifierResource(TRYGDERETTEN_ORGNUMMER, AVTALTMELDING_PROCESS);
		final Optional<ServiceRecord> anyServiceRecord = identifierResource.findServiceRecord(AVTALTMELDING_PROCESS, DPO);
		final ServiceRecord serviceRecord = anyServiceRecord
				.orElseThrow(() -> new MottakerInfoIkkeFunnetException("Fant ikke mottakerinfo for organisasjon=" + TRYGDERETTEN_ORGNUMMER + " og prosess=" + AVTALTMELDING_PROCESS));
		final Service service = serviceRecord.getService();
		return new MottakerInfo(serviceRecord.getOrganisationNumber(),
				serviceRecord.getPemCertificate(),
				service.getServiceCode(),
				service.getServiceEditionCode());
	}
}
