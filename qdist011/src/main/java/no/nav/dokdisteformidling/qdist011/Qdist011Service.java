package no.nav.dokdisteformidling.qdist011;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.qdist011.Qdist011FunctionalUtils.getDokumenttypeIdHoveddokument;
import static no.nav.dokdisteformidling.qdist011.Qdist011FunctionalUtils.validateForsendelseStatus;

import com.amazonaws.SdkClientException;
import no.nav.dokdisteformidling.consumer.dki.DigitalKontaktinformasjonV1;
import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat020.DokumentkatalogAdmin;
import no.nav.dokdisteformidling.consumer.dokkat.tkat020.DokumenttypeInfoTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfoTo;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.exception.functional.KunneIkkeDeserialisereS3JsonPayloadFunctionalException;
import no.nav.dokdisteformidling.qdist011.domain.DistribuerForsendelseTilDpi;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;
import no.nav.dokdisteformidling.storage.Storage;
import no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1.SendDigitalPost;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */
@Component
public class Qdist011Service {

	private final DokumentkatalogAdmin dokumentkatalogAdmin;
	private final VarselInfo varselInfo;
	private final AdministrerForsendelse administrerForsendelse;
	private final Storage storage;
	private final DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1;
	private final ProducerTemplate producer;
	private final BridgeMotSDPMapper bridgeMotSDPMapper;

	@Inject
	public Qdist011Service(DokumentkatalogAdmin dokumentkatalogAdmin,
						   VarselInfo varselInfo,
						   AdministrerForsendelse administrerForsendelse,
						   Storage storage,
						   DigitalKontaktinformasjonV1 digitalKontaktinformasjonV1,
						   ProducerTemplate producer, BridgeMotSDPMapper bridgeMotSDPMapper){
		this.dokumentkatalogAdmin = dokumentkatalogAdmin;
		this.varselInfo = varselInfo;
		this.administrerForsendelse = administrerForsendelse;
		this.storage = storage;
		this.digitalKontaktinformasjonV1 = digitalKontaktinformasjonV1;
		this.producer = producer;
		this.bridgeMotSDPMapper = bridgeMotSDPMapper;
	}

	@Handler
	public SendDigitalPost distribuerForsendelseTilDPIService(DistribuerForsendelseTilDpi distribuerForsendelseTilDpi, Exchange exchange){

		HentForsendelseResponseTo hentForsendelseResponseTo = administrerForsendelse.hentForsendelse(distribuerForsendelseTilDpi
				.getForsendelseId());
		validateForsendelseStatus(hentForsendelseResponseTo.getForsendelseStatus());

		HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo =
				digitalKontaktinformasjonV1.hentSikkerDigitalPostadresse(hentForsendelseResponseTo.getMottaker().getMottakerId());

		//Skal det ikke være et TO-objekt?
		DokumenttypeInfoTo dokumenttypeInfoTo = dokumentkatalogAdmin.getDokumenttypeInfo(getDokumenttypeIdHoveddokument(hentForsendelseResponseTo));

		VarselInfoTo varselInfoTo = varselInfo.getVarselInfo(dokumenttypeInfoTo.getVarselTypeId());		//Denne sjekker vel også at det er en valid varselinfo?

		//. Valider kontaktinformasjon (etter 3.)

		List<DokdistDokument> dokdistDokumentList = getDocumentsFromS3(hentForsendelseResponseTo);
		// last opp dokumenter via sftp til filkanal slik at sdp-kanal kan hente de
		for (DokdistDokument dokdistDokument : dokdistDokumentList) {
			producer.sendBody(LastOppDokumentRoute.ROUTE, dokdistDokument);
		}

		//. distribuer forsendelse - i router!?
		//input: forsendelse fra steg 1, digitalkontaktinfo fra steg 3, distribusjonsinfo fra steg 4, varselinfo fra steg 5.
		//input: hentForsendelseResponseTo, diginfo, dokumenttypeInfoTo, varselInfoTo
		//Dette er kanskje output fra hele servicefunksjonen?

		//. Oppdater forsendelsesstatus: I Router

		return bridgeMotSDPMapper.map(hentForsendelseResponseTo, hentSikkerDigitalPostadresseResponseTo, dokumenttypeInfoTo, varselInfoTo);
	}

	private List<DokdistDokument> getDocumentsFromS3(HentForsendelseResponseTo hentForsendelseResponseTo) {
		return hentForsendelseResponseTo.getDokumenter().stream()
				.map(dokumentTo -> {
					String jsonPayload = storage.get(dokumentTo.getDokumentObjektReferanse());
					return deserializeS3JsonPayloadToDokdistDokument(jsonPayload, dokumentTo.getDokumentObjektReferanse());
				})
				.collect(Collectors.toList());
	}

	private DokdistDokument deserializeS3JsonPayloadToDokdistDokument(String jsonPayload, String objektReferanse) {
		DokdistDokument dokdistDokument;
		try {
			dokdistDokument = JsonSerializer.deserialize(jsonPayload, DokdistDokument.class);
			dokdistDokument.setDokumentObjektReferanse(objektReferanse);
		} catch (SdkClientException e) {
			throw new KunneIkkeDeserialisereS3JsonPayloadFunctionalException(format("Kunne ikke deserialisere jsonPayload fra s3 bucket for dokument med dokumentobjektreferanse=%s. Dokumentet er ikke persistert til s3 med korrekt format!", objektReferanse));
		}
		return dokdistDokument;
	}
}
