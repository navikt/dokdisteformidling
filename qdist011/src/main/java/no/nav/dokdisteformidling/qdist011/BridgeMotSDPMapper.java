package no.nav.dokdisteformidling.qdist011;

import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.BUSINESS_SCOPE_TYPE;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.DIGITAL_POST;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.HOVEDDOKUMENT_HREF;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.HOVEDDOKUMENT_MIME;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.ORGANISASJON_IDENTIFIER;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.STANDARD;
import static no.nav.dokdisteformidling.constants.BridgeMotSDPMapperConstants.VERSION;

import no.difi.begrep.sdp.schema_v10.Avsender;
import no.difi.begrep.sdp.schema_v10.DigitalPost;
import no.difi.begrep.sdp.schema_v10.DigitalPostInfo;
import no.difi.begrep.sdp.schema_v10.Dokument;
import no.difi.begrep.sdp.schema_v10.EpostVarsel;
import no.difi.begrep.sdp.schema_v10.Manifest;
import no.difi.begrep.sdp.schema_v10.Mottaker;
import no.difi.begrep.sdp.schema_v10.Organisasjon;
import no.difi.begrep.sdp.schema_v10.Person;
import no.difi.begrep.sdp.schema_v10.SmsVarsel;
import no.difi.begrep.sdp.schema_v10.Tittel;
import no.difi.begrep.sdp.schema_v10.Varsler;
import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat020.DokumenttypeInfoTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfoTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeHenteDagensDatoTechnicalException;
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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */

@Component
public class BridgeMotSDPMapper {

	public SendDigitalPost map(HentForsendelseResponseTo hentforsendelseResponsTo, HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
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
		//tittelVedlegg.setValue(); //Må beskrives

		Mottaker mottaker = new Mottaker();

		Person person = new Person();
		person.setPersonidentifikator(hentforsendelseResponsTo.getMottaker().getMottakerId());
		person.setPostkasseadresse(hentSikkerDigitalPostadresseResponseTo.getSikkerDigitalPostkasse().getBrukerAdresse());

		Dokument hoveddokument = new Dokument();
		Dokument vedlegg = new Dokument();

		hoveddokument.setHref(HOVEDDOKUMENT_HREF);
		hoveddokument.setMime(HOVEDDOKUMENT_MIME);
		hoveddokument.setTittel(tittelHoveddokument);

		//Må beskrives
		//vedlegg.setHref();
		//vedlegg.setMime();

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

		//Set sender og mottaker finnes ikke.
		StandardBusinessDocumentHeader standardBusinessDocumentHeader = new StandardBusinessDocumentHeader();
		standardBusinessDocumentHeader.setHeaderVersion(VERSION);
		standardBusinessDocumentHeader.setDocumentIdentification(dokumentIdentificator);
		standardBusinessDocumentHeader.setBusinessScope(businessScope);

		Partner sender = new Partner();
		PartnerIdentification senderIdentification = new PartnerIdentification();
		senderIdentification.setValue(ORGANISASJON_IDENTIFIER);
		Partner receiver = new Partner();
		PartnerIdentification receiverIdentification = new PartnerIdentification();
		receiverIdentification.setValue(hentSikkerDigitalPostadresseResponseTo.getSikkerDigitalPostkasse().getLeverandoerAdresse());

		List<Partner> senderList = new ArrayList<Partner>();
		senderList.add(sender);
		List<Partner> receiverList = new ArrayList<Partner>();
		receiverList.add(receiver);

		//Fungerer dette?
		senderList = standardBusinessDocumentHeader.getSender();
		receiverList = standardBusinessDocumentHeader.getReceiver();

		DigitalPostInfo digitalPostInfo = new DigitalPostInfo();
		digitalPostInfo.setAapningskvittering(false);
		digitalPostInfo.setSikkerhetsnivaa(Integer.toString(dokumenttypeInfoTo.getSikkerhetsnivaa()));

		Tittel forsendelseTittel = new Tittel();
		forsendelseTittel.setValue(hentforsendelseResponsTo.getForsendelseTittel());
		digitalPostInfo.setIkkeSensitivTittel(forsendelseTittel);

		Varsler varsler = new Varsler();
		EpostVarsel epostVarsel = new EpostVarsel();
		epostVarsel.setEpostadresse(hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().getEpostadresse().getValue());
		//epostVarsel.setVarslingsTekst();    //Må beskrives
		//epostVarsel.setSpraakKode();	//Eksisterer ikke
		//epostVarsel.setAntallDagerListe();	//Eksisterer ikke

		SmsVarsel smsVarsel = new SmsVarsel();
		smsVarsel.setMobiltelefonnummer(hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon().getMobiltelefonnummer().getValue());
		//smsVarsel.setVarslingsTekst();    //Må beskrives
		//smsVarsel.antallDagerListe(); //Eksisterer ikke?

		varsler.setEpostVarsel(epostVarsel);
		varsler.setSmsVarsel(smsVarsel);
		digitalPostInfo.setVarsler(varsler);

		DigitalPost digitalPost = new DigitalPost();
		digitalPost.setAvsender(avsender);
		digitalPost.setMottaker(mottaker);

		digitalPost.setDigitalPostInfo(digitalPostInfo);

		StandardBusinessDocument standardBusinessDocument = new StandardBusinessDocument();
		standardBusinessDocument.setStandardBusinessDocumentHeader(standardBusinessDocumentHeader);

		standardBusinessDocument.setAny(digitalPost);        //Er det riktig å bruke setAny?

		//sendDigitalPostRequest.setStandardBusinessDocument();
		sendDigitalPostRequest.setManifest(manifest);
		sendDigitalPostRequest.setSertifikat(hentSikkerDigitalPostadresseResponseTo.getSertifikat());

		sendDigitalPostRequest.setErPrioritert(false);
		sendDigitalPost.setSendDigitalPostRequest(sendDigitalPostRequest);

		return sendDigitalPost;
	}

	private XMLGregorianCalendar getNow() {
		XMLGregorianCalendar now;
		try {
			now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
		} catch (DatatypeConfigurationException e) {
			throw new KunneIkkeHenteDagensDatoTechnicalException("qdist011 kunne ikke hente dagens dato", e);
		}
		return now;
	}

}
