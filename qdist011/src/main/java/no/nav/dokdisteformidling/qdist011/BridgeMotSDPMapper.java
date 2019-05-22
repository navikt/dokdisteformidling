package no.nav.dokdisteformidling.qdist011;

import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.BUSINESS_SCOPE_TYPE;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.DIGITAL_POST;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.DOKUMENT_MIME;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.EPOST;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.ORGANISASJON_IDENTIFIER;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.SMS;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.STANDARD;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.VERSION;
import static no.nav.dokdisteformidling.qdist011.Qdist011FunctionalUtils.getNow;

import no.difi.begrep.sdp.schema_v10.Avsender;
import no.difi.begrep.sdp.schema_v10.DigitalPost;
import no.difi.begrep.sdp.schema_v10.DigitalPostInfo;
import no.difi.begrep.sdp.schema_v10.Dokument;
import no.difi.begrep.sdp.schema_v10.EpostVarsel;
import no.difi.begrep.sdp.schema_v10.EpostVarselTekst;
import no.difi.begrep.sdp.schema_v10.Manifest;
import no.difi.begrep.sdp.schema_v10.Mottaker;
import no.difi.begrep.sdp.schema_v10.Organisasjon;
import no.difi.begrep.sdp.schema_v10.Person;
import no.difi.begrep.sdp.schema_v10.SmsVarsel;
import no.difi.begrep.sdp.schema_v10.SmsVarselTekst;
import no.difi.begrep.sdp.schema_v10.Tittel;
import no.difi.begrep.sdp.schema_v10.Varsler;
import no.nav.dokdisteformidling.constants.DomainConstants;
import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat020.DokumenttypeInfoTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfoTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.consumer.saf.journalpost.Journalpost;
import no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1.ObjectFactory;
import no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1.SendDigitalPost;
import no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1.meldinger.SendDigitalPostRequest;
import org.springframework.stereotype.Component;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.BusinessScope;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.DocumentIdentification;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Partner;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.PartnerIdentification;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Scope;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocument;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import java.util.List;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */

@Component
public class BridgeMotSDPMapper {

	public SendDigitalPost map(HentForsendelseResponseTo hentForsendelseResponsTo,
							   HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
							   DokumenttypeInfoTo dokumenttypeInfoTo, VarselInfoTo varselInfoTo,
							   Journalpost journalpost) {
		ObjectFactory digitalPostOF = new ObjectFactory();
		SendDigitalPost sendDigitalPost = digitalPostOF.createSendDigitalPost();
		SendDigitalPostRequest sendDigitalPostRequest = new SendDigitalPostRequest();

		StandardBusinessDocument standardBusinessDocument = new StandardBusinessDocument();
		standardBusinessDocument.setStandardBusinessDocumentHeader(mapStandardBusinessDocumentHeader(
				hentSikkerDigitalPostadresseResponseTo, hentForsendelseResponsTo));

		standardBusinessDocument.setAny(mapDigitalPost(hentSikkerDigitalPostadresseResponseTo, hentForsendelseResponsTo,
				dokumenttypeInfoTo, varselInfoTo));

		sendDigitalPostRequest.setStandardBusinessDocument(standardBusinessDocument);
		sendDigitalPostRequest.setManifest(mapManifest(hentSikkerDigitalPostadresseResponseTo, hentForsendelseResponsTo, journalpost));
		sendDigitalPostRequest.setSertifikat(hentSikkerDigitalPostadresseResponseTo.getSertifikat());
		sendDigitalPostRequest.setErPrioritert(false);
		sendDigitalPost.setSendDigitalPostRequest(sendDigitalPostRequest);

		return sendDigitalPost;
	}

	private StandardBusinessDocumentHeader mapStandardBusinessDocumentHeader(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
																			 HentForsendelseResponseTo hentForsendelseResponseTo) {
		DocumentIdentification dokumentIdentificator = new DocumentIdentification();
		dokumentIdentificator.setStandard(STANDARD);
		dokumentIdentificator.setTypeVersion(VERSION);
		dokumentIdentificator.setInstanceIdentifier(hentForsendelseResponseTo.getBestillingsId());
		dokumentIdentificator.setType(DIGITAL_POST);
		dokumentIdentificator.setCreationDateAndTime(getNow());

		Scope scope = new Scope();
		scope.setType(BUSINESS_SCOPE_TYPE);
		scope.setInstanceIdentifier(hentForsendelseResponseTo.getBestillingsId());
		scope.setIdentifier(STANDARD);
		BusinessScope businessScope = new BusinessScope();

		List <Scope> scopeList = businessScope.getScope();
		scopeList.add(scope);

		StandardBusinessDocumentHeader standardBusinessDocumentHeader = new StandardBusinessDocumentHeader();
		standardBusinessDocumentHeader.setHeaderVersion(VERSION);
		standardBusinessDocumentHeader.setDocumentIdentification(dokumentIdentificator);
		standardBusinessDocumentHeader.setBusinessScope(businessScope);

		Partner sender = new Partner();
		PartnerIdentification senderIdentification = new PartnerIdentification();
		senderIdentification.setValue(ORGANISASJON_IDENTIFIER);
		Partner receiver = new Partner();
		PartnerIdentification receiverIdentification = new PartnerIdentification();
		receiverIdentification.setValue(hentSikkerDigitalPostadresseResponseTo.getSikkerDigitalPostkasse()
				.getLeverandoerAdresse());

		List<Partner> senderList = standardBusinessDocumentHeader.getSender();
		senderList.add(sender);
		List<Partner> receiverList = standardBusinessDocumentHeader.getReceiver();
		receiverList.add(receiver);

		return standardBusinessDocumentHeader;
	}

	private DigitalPost mapDigitalPost(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
									   HentForsendelseResponseTo hentForsendelseResponseTo,
									   DokumenttypeInfoTo dokumenttypeInfoTo,
									   VarselInfoTo varselInfoTo) {
		DigitalPost digitalPost = new DigitalPost();

		DigitalPostInfo digitalPostInfo = new DigitalPostInfo();
		digitalPostInfo.setAapningskvittering(false);
		digitalPostInfo.setSikkerhetsnivaa(Integer.toString(dokumenttypeInfoTo.getSikkerhetsnivaa()));

		Tittel forsendelseTittel = new Tittel();
		forsendelseTittel.setValue(hentForsendelseResponseTo.getForsendelseTittel());
		digitalPostInfo.setIkkeSensitivTittel(forsendelseTittel);

		Varsler varsler = mapVarsler(varselInfoTo, hentSikkerDigitalPostadresseResponseTo);
		digitalPostInfo.setVarsler(varsler);

		digitalPost.setAvsender(mapAvsender());
		digitalPost.setMottaker(mapMottaker(hentSikkerDigitalPostadresseResponseTo, hentForsendelseResponseTo));
		digitalPost.setDigitalPostInfo(digitalPostInfo);

		return digitalPost;
	}

	private Mottaker mapMottaker(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
								 HentForsendelseResponseTo hentForsendelseResponseTo) {
		Mottaker mottaker = new Mottaker();

		Person person = new Person();
		person.setPersonidentifikator(hentForsendelseResponseTo.getMottaker().getMottakerId());
		person.setPostkasseadresse(hentSikkerDigitalPostadresseResponseTo.getSikkerDigitalPostkasse().getBrukerAdresse());

		mottaker.setPerson(person);

		return mottaker;
	}

	private Avsender mapAvsender() {
		Organisasjon organisasjon = new Organisasjon();
		organisasjon.setValue(ORGANISASJON_IDENTIFIER);
		Avsender avsender = new Avsender();
		avsender.setOrganisasjon(organisasjon);

		return avsender;
	}

	private Manifest mapManifest(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
								 HentForsendelseResponseTo hentForsendelseResponseTo,
								 Journalpost journalpost) {
		Tittel tittelHoveddokument = new Tittel();
		tittelHoveddokument.setValue(hentForsendelseResponseTo.getForsendelseTittel());
		Dokument hoveddokument = new Dokument();
		hoveddokument.setHref(hentForsendelseResponseTo.getDokumenter().stream()
				.filter(DokumentTo -> DomainConstants.HOVEDDOKUMENT.equals(DokumentTo.getTilknyttetSom()))
				.findAny()
				.map(HentForsendelseResponseTo.DokumentTo::getDokumentURI).get());
		hoveddokument.setMime(DOKUMENT_MIME);
		hoveddokument.setTittel(tittelHoveddokument);

		Manifest manifest = new Manifest();
		manifest.setAvsender(mapAvsender());
		manifest.setMottaker(mapMottaker(hentSikkerDigitalPostadresseResponseTo, hentForsendelseResponseTo));
		manifest.setHoveddokument(hoveddokument);

		List<Dokument> vedlegg = manifest.getVedlegg();

		hentForsendelseResponseTo.getDokumenter().stream()
				.filter(DokumentTo -> (DomainConstants.VEDLEGG.equals(DokumentTo.getTilknyttetSom())))
				.forEach(dokumentTo -> {
					Dokument dokumentVedlegg = new Dokument();
					Tittel tittelVedlegg = new Tittel();

					tittelVedlegg.setValue(
							journalpost.getDokumenter().stream()
							.filter(dokumentInfo -> dokumentInfo.getDokumentInfoId().equals(dokumentTo.getArkivDokumentInfoId()))
							.findFirst()
							.get().getTittel()
					);
					dokumentVedlegg.setTittel(tittelVedlegg);
					dokumentVedlegg.setHref(dokumentTo.getDokumentURI());
					dokumentVedlegg.setMime(DOKUMENT_MIME);
					vedlegg.add(dokumentVedlegg);
				});

		return manifest;
	}

	private boolean isPreferertKanalEpost(VarselInfoTo varselInfoTo){
		return varselInfoTo.getPreferertKanal().stream()
				.filter(preferertKanal -> EPOST.equals(preferertKanal))
				.findAny()
				.isPresent();
	}

	private boolean isPreferertKanalMobil(VarselInfoTo varselInfoTo){
		return varselInfoTo.getPreferertKanal().stream()
				.filter(preferertKanal -> SMS.equals(preferertKanal))
				.findAny()
				.isPresent();
	}

	private Varsler mapVarsler(VarselInfoTo varselInfoTo,
							   HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) {

		Varsler varsler = null;

		boolean isEpostDateInvalid = DigitalKontaktInformasjonValidator.isEpostDateInvalid(hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().getEpostadresse());
		boolean isMobilDateInvalid = DigitalKontaktInformasjonValidator.isMobilDateInvalid(hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().getMobiltelefonnummer());

		if ((isPreferertKanalEpost(varselInfoTo) || isMobilDateInvalid) && !isEpostDateInvalid){
			varsler = new Varsler();
			EpostVarsel epostVarsel = new EpostVarsel();
			EpostVarselTekst epostVarselTekst = new EpostVarselTekst();
			epostVarselTekst.setValue(varselInfoTo.getVarslingsTekst());
			epostVarsel.setEpostadresse(hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon()
					.getEpostadresse()
					.toString());
			epostVarsel.setVarslingsTekst(epostVarselTekst);
			varsler.setEpostVarsel(epostVarsel);
		}

		if ((isPreferertKanalMobil(varselInfoTo) || isEpostDateInvalid) && !isMobilDateInvalid) {
			if(varsler == null){
				varsler = new Varsler();
			}
			SmsVarsel smsVarsel = new SmsVarsel();
			smsVarsel.setMobiltelefonnummer(hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon()
					.getMobiltelefonnummer()
					.toString());
			SmsVarselTekst smsVarselTekst = new SmsVarselTekst();
			smsVarselTekst.setValue(varselInfoTo.getVarslingsTekst());
			smsVarsel.setVarslingsTekst(smsVarselTekst);
			varsler.setSmsVarsel(smsVarsel);
		}
		return varsler;
	}
}
