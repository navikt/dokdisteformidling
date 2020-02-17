package no.nav.dokdisteformidling.qdist011;

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
import no.difi.begrep.sdp.schema_v10.Repetisjoner;
import no.difi.begrep.sdp.schema_v10.SmsVarsel;
import no.difi.begrep.sdp.schema_v10.SmsVarselTekst;
import no.difi.begrep.sdp.schema_v10.Tittel;
import no.difi.begrep.sdp.schema_v10.Varsler;
import no.nav.dokdisteformidling.constants.DomainConstants;
import no.nav.dokdisteformidling.consumer.dki.HentSikkerDigitalPostadresseResponseTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat020.DokumenttypeInfoTo;
import no.nav.dokdisteformidling.consumer.dokkat.tkat021.VarselInfoTo;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponseTo;
import no.nav.dokdisteformidling.exception.functional.Tdist005MapperFunctionalException;
import no.nav.dokdisteformidling.qdist011.saf.JournalpostQdist011;
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

import javax.xml.bind.JAXBElement;
import java.util.List;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.AUTHORITY;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.AUTHORITY_ENUM;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.BUSINESS_SCOPE_TYPE;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.DIGITAL_POST;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.DOKUMENT_MIME;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.EPOST;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.ORGNR_NAV;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.ORG_PREFIX;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.SMS;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.SPRAAK_KODE;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.STANDARD;
import static no.nav.dokdisteformidling.qdist011.constants.BridgeMotSDPMapperConstants.VERSION;
import static no.nav.dokdisteformidling.utils.FunctionalUtils.getNow;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */

@Component
public class BridgeMotSDPMapper {

	public SendDigitalPost map(HentForsendelseResponseTo hentForsendelseResponsTo,
							   HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
							   DokumenttypeInfoTo dokumenttypeInfoTo, VarselInfoTo varselInfoTo,
							   JournalpostQdist011 journalpostQdist011) {
		try {
			ObjectFactory digitalPostOF = new ObjectFactory();
			SendDigitalPost sendDigitalPost = digitalPostOF.createSendDigitalPost();
			SendDigitalPostRequest sendDigitalPostRequest = new SendDigitalPostRequest();

			StandardBusinessDocument standardBusinessDocument = new StandardBusinessDocument();
			standardBusinessDocument.setStandardBusinessDocumentHeader(mapStandardBusinessDocumentHeader(
					hentSikkerDigitalPostadresseResponseTo, hentForsendelseResponsTo));

			DigitalPost digitalPost = mapDigitalPost(hentSikkerDigitalPostadresseResponseTo, hentForsendelseResponsTo,
					dokumenttypeInfoTo, varselInfoTo);
			// create jaxbwrapper for Digitalpost to avoid needing @XmlRootElement annotation
			JAXBElement<DigitalPost> jaxbDigitalPost = new no.difi.begrep.sdp.schema_v10.ObjectFactory().createDigitalPost(digitalPost);
			standardBusinessDocument.setAny(jaxbDigitalPost);

			sendDigitalPostRequest.setStandardBusinessDocument(standardBusinessDocument);
			sendDigitalPostRequest.setManifest(mapManifest(hentSikkerDigitalPostadresseResponseTo, hentForsendelseResponsTo, journalpostQdist011));
			sendDigitalPostRequest.setSertifikat(hentSikkerDigitalPostadresseResponseTo.getSertifikat());
			sendDigitalPostRequest.setErPrioritert(false);
			sendDigitalPost.setSendDigitalPostRequest(sendDigitalPostRequest);

			return sendDigitalPost;
		} catch (Exception e) {
			throw new Tdist005MapperFunctionalException(format("Kunne ikke mappe qdist011 output mot tdist005. Feilmelding=%s",
					e.getMessage()), e);
		}
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

		List<Scope> scopeList = businessScope.getScope();
		scopeList.add(scope);

		StandardBusinessDocumentHeader standardBusinessDocumentHeader = new StandardBusinessDocumentHeader();
		standardBusinessDocumentHeader.setHeaderVersion(VERSION);
		standardBusinessDocumentHeader.setDocumentIdentification(dokumentIdentificator);
		standardBusinessDocumentHeader.setBusinessScope(businessScope);

		Partner sender = new Partner();
		PartnerIdentification senderIdentification = new PartnerIdentification();
		senderIdentification.setValue(ORGNR_NAV);
		senderIdentification.setAuthority(AUTHORITY);
		sender.setIdentifier(senderIdentification);
		Partner receiver = new Partner();
		PartnerIdentification receiverIdentification = new PartnerIdentification();
		String leverandoerAdresse = hentSikkerDigitalPostadresseResponseTo.getSikkerDigitalPostkasse().getLeverandoerAdresse();
		if (leverandoerAdresse != null && !leverandoerAdresse.startsWith(ORG_PREFIX)) {
			leverandoerAdresse = ORG_PREFIX + leverandoerAdresse;
		}
		receiverIdentification.setValue(leverandoerAdresse);
		receiverIdentification.setAuthority(AUTHORITY);
		receiver.setIdentifier(receiverIdentification);

		standardBusinessDocumentHeader.getSender().add(sender);
		standardBusinessDocumentHeader.getReceiver().add(receiver);

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
		forsendelseTittel.setLang(SPRAAK_KODE);
		digitalPostInfo.setIkkeSensitivTittel(forsendelseTittel);

		if (varselInfoTo != null) {
			Varsler varsler = mapVarsler(varselInfoTo, hentSikkerDigitalPostadresseResponseTo);
			digitalPostInfo.setVarsler(varsler);
		}

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
		organisasjon.setValue(ORGNR_NAV);
		organisasjon.setAuthority(AUTHORITY_ENUM);
		Avsender avsender = new Avsender();
		avsender.setOrganisasjon(organisasjon);
		return avsender;
	}

	private Manifest mapManifest(HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo,
								 HentForsendelseResponseTo hentForsendelseResponseTo,
								 JournalpostQdist011 journalpostQdist011) {
		Tittel tittelHoveddokument = new Tittel();
		tittelHoveddokument.setValue(hentForsendelseResponseTo.getForsendelseTittel());
		tittelHoveddokument.setLang(SPRAAK_KODE);
		Dokument hoveddokument = new Dokument();
		hoveddokument.setHref(hentForsendelseResponseTo.getDokumenter().stream()
				.filter(dokumentTo -> DomainConstants.HOVEDDOKUMENT.equals(dokumentTo.getTilknyttetSom()))
				.findAny()
				.map(HentForsendelseResponseTo.DokumentTo::getDokumentObjektReferanse)
				.orElseThrow(() -> new Tdist005MapperFunctionalException(
						format("Kunne ikke finne hoveddokument for bestilling med bestillingsId=%s",
								hentForsendelseResponseTo.getBestillingsId())))
				.concat(".pdf"));
		hoveddokument.setMime(DOKUMENT_MIME);
		hoveddokument.setTittel(tittelHoveddokument);

		Manifest manifest = new Manifest();
		manifest.setAvsender(mapAvsender());
		manifest.setMottaker(mapMottaker(hentSikkerDigitalPostadresseResponseTo, hentForsendelseResponseTo));
		manifest.setHoveddokument(hoveddokument);

		hentForsendelseResponseTo.getDokumenter().stream()
				.filter(dokumentTo -> (DomainConstants.VEDLEGG.equals(dokumentTo.getTilknyttetSom())))
				.forEach(dokumentTo -> {
					Dokument dokumentVedlegg = new Dokument();
					Tittel tittelVedlegg = new Tittel();

					tittelVedlegg.setValue(
							journalpostQdist011.getDokumenter().stream()
									.filter(dokumentInfo -> dokumentInfo.getDokumentInfoId()
											.equals(dokumentTo.getArkivDokumentInfoId()))
									.findAny()
									.orElseThrow(() -> new Tdist005MapperFunctionalException(
											String.format("DokumentInfoId=%s ikke funnet i journalpost",
													dokumentTo.getArkivDokumentInfoId())))
									.getTittel()
					);
					tittelVedlegg.setLang(SPRAAK_KODE);
					dokumentVedlegg.setTittel(tittelVedlegg);
					dokumentVedlegg.setHref(dokumentTo.getDokumentObjektReferanse().concat(".pdf"));
					dokumentVedlegg.setMime(DOKUMENT_MIME);
					manifest.getVedlegg().add(dokumentVedlegg);
				});

		return manifest;
	}

	private Varsler mapVarsler(VarselInfoTo varselInfoTo, HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) {

		Varsler varsler = null;

		if (sendEpostVarsel(varselInfoTo, hentSikkerDigitalPostadresseResponseTo)) {
			varsler = new Varsler();
			varsler.setEpostVarsel(createEpostVarsler(varselInfoTo, hentSikkerDigitalPostadresseResponseTo));
		}

		if (sendSMSVarsel(varselInfoTo, hentSikkerDigitalPostadresseResponseTo)) {
			if (varsler == null) {
				varsler = new Varsler();
			}
			varsler.setSmsVarsel(createSMSVarsler(varselInfoTo, hentSikkerDigitalPostadresseResponseTo));
		}
		return varsler;
	}

	private EpostVarsel createEpostVarsler(VarselInfoTo varselInfoTo, HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) {
		EpostVarsel epostVarsel = new EpostVarsel();

		epostVarsel.setEpostadresse(hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon()
				.getEpostadresse()
				.getValue());
		EpostVarselTekst epostVarselTekst = new EpostVarselTekst();
		epostVarselTekst.setValue(varselInfoTo.getVarslingsTekst().get(EPOST));
		epostVarselTekst.setLang(SPRAAK_KODE);
		epostVarsel.setVarslingsTekst(epostVarselTekst);
		epostVarsel.setRepetisjoner(createRepetisjoner(varselInfoTo));

		return epostVarsel;
	}

	private SmsVarsel createSMSVarsler(VarselInfoTo varselInfoTo, HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) {
		SmsVarsel smsVarsel = new SmsVarsel();
		smsVarsel.setMobiltelefonnummer(hentSikkerDigitalPostadresseResponseTo.getDigitalKontaktinformasjon()
				.getMobiltelefonnummer()
				.getValue());
		SmsVarselTekst smsVarselTekst = new SmsVarselTekst();
		smsVarselTekst.setValue(varselInfoTo.getVarslingsTekst().get(SMS));
		smsVarselTekst.setLang(SPRAAK_KODE);
		smsVarsel.setVarslingsTekst(smsVarselTekst);
		smsVarsel.setRepetisjoner(createRepetisjoner(varselInfoTo));

		return smsVarsel;
	}

	private Repetisjoner createRepetisjoner(VarselInfoTo varselInfoTo) {
		Repetisjoner repitisjoner = null;

		if (varselInfoTo.getAntallDagerListe() != null) {
			repitisjoner = new Repetisjoner();
			repitisjoner.getDagerEtter().addAll(varselInfoTo.getAntallDagerListe());
		}
		return repitisjoner;
	}

	private boolean sendSMSVarsel(VarselInfoTo varselInfoTo, HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) {

		boolean isEpostInvalid = DigitalKontaktInformasjonValidator.isEpostInvalid(hentSikkerDigitalPostadresseResponseTo
				.getDigitalKontaktinformasjon()
				.getEpostadresse());

		boolean isMobilInvalid = DigitalKontaktInformasjonValidator.isMobilInvalid(hentSikkerDigitalPostadresseResponseTo
				.getDigitalKontaktinformasjon()
				.getMobiltelefonnummer());

		return (isPreferertKanalMobil(varselInfoTo) || isEpostInvalid) && !isMobilInvalid;
	}

	private boolean sendEpostVarsel(VarselInfoTo varselInfoTo, HentSikkerDigitalPostadresseResponseTo hentSikkerDigitalPostadresseResponseTo) {

		boolean isEpostInvalid = DigitalKontaktInformasjonValidator.isEpostInvalid(hentSikkerDigitalPostadresseResponseTo
				.getDigitalKontaktinformasjon()
				.getEpostadresse());

		boolean isMobilInvalid = DigitalKontaktInformasjonValidator.isMobilInvalid(hentSikkerDigitalPostadresseResponseTo
				.getDigitalKontaktinformasjon()
				.getMobiltelefonnummer());

		return (isPreferertKanalEpost(varselInfoTo) || isMobilInvalid) && !isEpostInvalid;
	}

	private boolean isPreferertKanalEpost(VarselInfoTo varselInfoTo) {
		return varselInfoTo.getPreferertKanal().stream()
				.anyMatch(EPOST::equals);
	}

	private boolean isPreferertKanalMobil(VarselInfoTo varselInfoTo) {
		return varselInfoTo.getPreferertKanal().stream()
				.anyMatch(SMS::equals);
	}
}
