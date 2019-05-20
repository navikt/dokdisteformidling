package no.nav.dokdisteformidling.qdist011;

import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.BUSINESS_SCOPE_TYPE;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.DIGITAL_POST;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.HOVEDDOKUMENT_MIME;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.ORGANISASJON_IDENTIFIER;
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
import no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1.ObjectFactory;
import no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1.SendDigitalPost;
import no.nav.tjeneste.virksomhet.digitalpost.senddigitalpost.v1.meldinger.SendDigitalPostRequest;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.BusinessScope;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.DocumentIdentification;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Partner;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.PartnerIdentification;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Scope;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocument;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */

@Component
public class BridgeMotSDPMapper {

	@Handler
	public static SendDigitalPost map(HentForsendelseResponseTo hentforsendelseResponsTo, HentSikkerDigitalPostadresseResponseTo digitalKontaktInformasjon,
									  DokumenttypeInfoTo dokumenttypeInfoTo, VarselInfoTo varselInfoTo) {
		ObjectFactory digitalPostOF = new ObjectFactory();
		SendDigitalPost sendDigitalPost = digitalPostOF.createSendDigitalPost();
		SendDigitalPostRequest sendDigitalPostRequest = new SendDigitalPostRequest();

		Organisasjon organisasjon = new Organisasjon();
		organisasjon.setValue(ORGANISASJON_IDENTIFIER);
		Avsender avsender = new Avsender();
		avsender.setOrganisasjon(organisasjon);

		Tittel tittelHoveddokument = new Tittel();
		tittelHoveddokument.setValue(hentforsendelseResponsTo.getForsendelseTittel());
		Tittel tittelVedlegg = new Tittel();
		//Todo Må beskrives
		//tittelVedlegg.setValue();

		Mottaker mottaker = new Mottaker();

		Person person = new Person();
		person.setPersonidentifikator(hentforsendelseResponsTo.getMottaker().getMottakerId());
		person.setPostkasseadresse(digitalKontaktInformasjon.getSikkerDigitalPostkasse().getBrukerAdresse());

		Dokument hoveddokument = new Dokument();
		Dokument vedlegg = new Dokument();

		//Er dette riktig?
		//Må ha med feilhåndtering?
		hoveddokument.setHref(hentforsendelseResponsTo.getDokumenter().stream()
				.filter(DokumentTo -> DomainConstants.HOVEDDOKUMENT.equals(DokumentTo.getTilknyttetSom()))
				.findFirst()
				.map(HentForsendelseResponseTo.DokumentTo::getDokumentObjektReferanse)
				.toString() + ".pdf");
						/*.orElseThrow(() -> new Tkat021FunctionalException(format("Fant ingen distribusjonVarsel med varselForDistribusjonKanal=%s for dokumenttypeId=%s",
										DomainConstants.DISTRIBUSJONS_KANAL, varselInfoRestTo.getVarseltypeId()))); */

		hoveddokument.setMime(HOVEDDOKUMENT_MIME);
		hoveddokument.setTittel(tittelHoveddokument);

		vedlegg.setHref(hentforsendelseResponsTo.getDokumenter().stream()
				.filter(DokumentTo -> DomainConstants.HOVEDDOKUMENT.equals(DokumentTo.getTilknyttetSom()))
				.findFirst()
				.map(HentForsendelseResponseTo.DokumentTo::getDokumentObjektReferanse)
				.toString() + ".pdf");
		vedlegg.setMime(HOVEDDOKUMENT_MIME);

		//Todo Må beskrives
		//vedlegg.setTittel

		mottaker.setPerson(person);

		Manifest manifest = new Manifest();
		manifest.setAvsender(avsender);
		manifest.setMottaker(mottaker);
		manifest.setHoveddokument(hoveddokument);

		XMLGregorianCalendar now = getNow();

		DocumentIdentification dokumentIdentificator = new DocumentIdentification();
		dokumentIdentificator.setStandard(STANDARD);
		dokumentIdentificator.setTypeVersion(VERSION);
		dokumentIdentificator.setInstanceIdentifier(hentforsendelseResponsTo.getBestillingsId());
		dokumentIdentificator.setType(DIGITAL_POST);
		dokumentIdentificator.setCreationDateAndTime(now);

		Scope scope = new Scope();
		scope.setType(BUSINESS_SCOPE_TYPE);
		scope.setInstanceIdentifier(hentforsendelseResponsTo.getBestillingsId());
		scope.setIdentifier(STANDARD);
		BusinessScope businessScope = new BusinessScope();

		StandardBusinessDocumentHeader standardBusinessDocumentHeader = new StandardBusinessDocumentHeader();
		standardBusinessDocumentHeader.setHeaderVersion(VERSION);
		standardBusinessDocumentHeader.setDocumentIdentification(dokumentIdentificator);
		standardBusinessDocumentHeader.setBusinessScope(businessScope);

		Partner sender = new Partner();
		PartnerIdentification senderIdentification = new PartnerIdentification();
		senderIdentification.setValue(ORGANISASJON_IDENTIFIER);
		Partner receiver = new Partner();
		PartnerIdentification receiverIdentification = new PartnerIdentification();
		receiverIdentification.setValue(digitalKontaktInformasjon.getSikkerDigitalPostkasse().getLeverandoerAdresse());

		List<Partner> senderList = standardBusinessDocumentHeader.getSender();
		senderList.add(sender);
		List<Partner> receiverList = standardBusinessDocumentHeader.getReceiver();
		receiverList.add(receiver);

		DigitalPostInfo digitalPostInfo = new DigitalPostInfo();
		digitalPostInfo.setAapningskvittering(false);
		digitalPostInfo.setSikkerhetsnivaa(Integer.toString(dokumenttypeInfoTo.getSikkerhetsnivaa()));

		Tittel forsendelseTittel = new Tittel();
		forsendelseTittel.setValue(hentforsendelseResponsTo.getForsendelseTittel());
		digitalPostInfo.setIkkeSensitivTittel(forsendelseTittel);

		Varsler varsler = new Varsler();

		if (varselInfoTo.getPreferertKanal().equals(DomainConstants.EPOST) ||
				(digitalKontaktInformasjon.getDigitalKontaktinformasjon().getMobiltelefonnummer().equals(null) &&
						!digitalKontaktInformasjon.getDigitalKontaktinformasjon().getEpostadresse().equals(null))) {
			EpostVarsel epostVarsel = new EpostVarsel();
			EpostVarselTekst epostVarselTekst = new EpostVarselTekst();
			epostVarselTekst.setValue(varselInfoTo.getVarslingsTekst());
			epostVarsel.setEpostadresse(digitalKontaktInformasjon.getDigitalKontaktinformasjon().getEpostadresse().toString());
			epostVarsel.setVarslingsTekst(epostVarselTekst);
			varsler.setEpostVarsel(epostVarsel);
		} else if (varselInfoTo.getPreferertKanal().equals(DomainConstants.SMS) ||
				(!digitalKontaktInformasjon.getDigitalKontaktinformasjon().getMobiltelefonnummer().equals(null) &&
						digitalKontaktInformasjon.getDigitalKontaktinformasjon().getEpostadresse().equals(null))) {
			SmsVarsel smsVarsel = new SmsVarsel();
			smsVarsel.setMobiltelefonnummer(digitalKontaktInformasjon.getDigitalKontaktinformasjon()
					.getMobiltelefonnummer()
					.toString());
			SmsVarselTekst smsVarselTekst = new SmsVarselTekst();
			smsVarselTekst.setValue(varselInfoTo.getVarslingsTekst());
			smsVarsel.setVarslingsTekst(smsVarselTekst);
			varsler.setSmsVarsel(smsVarsel);
		}
		//Hva skjer hvis ingen er satt? Feilmelding?

		digitalPostInfo.setVarsler(varsler);

		DigitalPost digitalPost = new DigitalPost();
		digitalPost.setAvsender(avsender);
		digitalPost.setMottaker(mottaker);

		digitalPost.setDigitalPostInfo(digitalPostInfo);

		StandardBusinessDocument standardBusinessDocument = new StandardBusinessDocument();
		standardBusinessDocument.setStandardBusinessDocumentHeader(standardBusinessDocumentHeader);

		standardBusinessDocument.setAny(digitalPost);

		sendDigitalPostRequest.setStandardBusinessDocument(standardBusinessDocument);
		sendDigitalPostRequest.setManifest(manifest);
		sendDigitalPostRequest.setSertifikat(digitalKontaktInformasjon.getSertifikat());

		sendDigitalPostRequest.setErPrioritert(false);
		sendDigitalPost.setSendDigitalPostRequest(sendDigitalPostRequest);

		return sendDigitalPost;
	}


}
