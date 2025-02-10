package no.nav.dokdisteformidling.qdist013;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivmelding.Journalpost;
import no.nav.dokdisteformidling.consumer.eformidling.Eformidling;
import no.nav.dokdisteformidling.consumer.eformidling.NavDokumentpakke;
import no.nav.dokdisteformidling.consumer.juridisklogg.JuridiskLogg;
import no.nav.dokdisteformidling.consumer.juridisklogg.LagreJuridiskLoggMapper;
import no.nav.dokdisteformidling.consumer.rdist001.AdministrerForsendelse;
import no.nav.dokdisteformidling.consumer.rdist001.HentForsendelseResponse;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.exception.functional.IkkeSammenfallendeIderFunctionalException;
import no.nav.dokdisteformidling.exception.functional.InvalidForsendelseStatusFunctionalException;
import no.nav.dokdisteformidling.exception.functional.KunneIkkeDeserialisereBucketJsonPayloadFunctionalException;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeMarshalleArkivmeldingTechnicalException;
import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import no.nav.dokdisteformidling.storage.BucketStorage;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_KLAR_FOR_DIST;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_ANTALL_DOK;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_BESTILLINGS_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_CONVERSATION_ID;
import static no.nav.dokdisteformidling.constants.RouteConstants.PROPERTY_JOURNALPOST_ID;
import static no.nav.dokdisteformidling.consumer.eformidling.NavDokument.fromAvtaltmelding;
import static no.nav.dokdisteformidling.consumer.eformidling.NavDokument.fromVedlegg;

@Slf4j
@Service
public class Qdist013Service {

    private final BucketStorage bucketStorage;
    private final AdministrerForsendelse administrerForsendelse;
    private final SafJournalpostQueryService<JournalpostQdist013> safJournalpostQueryService;
    private final JuridiskLogg juridiskLogg;
    private final LagreJuridiskLoggMapper lagreJuridiskLoggMapper;
    private final AvtaltmeldingMapper avtaltmeldingMapper;
    private final Eformidling eformidling;

    public Qdist013Service(BucketStorage bucketStorage,
                           AdministrerForsendelse administrerForsendelse,
                           @Qualifier("SafJournalpostQueryServiceQdist013") SafJournalpostQueryService<JournalpostQdist013> safJournalpostQueryService,
                           JuridiskLogg juridiskLogg,
                           LagreJuridiskLoggMapper lagreJuridiskLoggMapper,
                           AvtaltmeldingMapper avtaltmeldingMapper,
                           Eformidling eformidling) {
        this.bucketStorage = bucketStorage;
        this.administrerForsendelse = administrerForsendelse;
        this.safJournalpostQueryService = safJournalpostQueryService;
        this.juridiskLogg = juridiskLogg;
        this.lagreJuridiskLoggMapper = lagreJuridiskLoggMapper;
        this.avtaltmeldingMapper = avtaltmeldingMapper;
        this.eformidling = eformidling;
    }

    @Handler
    public void processForsendelse(DistribuerForsendelseTilTrygderetten distribuerForsendelseTilTrygderetten, Exchange exchange) {
        final String conversationId = UUID.randomUUID().toString();
        exchange.setProperty(PROPERTY_CONVERSATION_ID, conversationId);

        final Long forsendelseId = Long.valueOf(distribuerForsendelseTilTrygderetten.forsendelseId());
        final HentForsendelseResponse hentForsendelseResponse = administrerForsendelse.hentForsendelse(forsendelseId);

        final String bestillingsId = hentForsendelseResponse.getBestillingsId();
        exchange.setProperty(PROPERTY_BESTILLINGS_ID, bestillingsId);

        final String journalpostId =  hentForsendelseResponse.getArkivInformasjon().getArkivId();
        exchange.setProperty(PROPERTY_JOURNALPOST_ID, journalpostId);
        exchange.setProperty(PROPERTY_CONVERSATION_ID, conversationId);
        validateThatForsendelseStatusIsKlarForDist(hentForsendelseResponse.getForsendelseStatus());

        final List<DokdistDokument> dokdistDokumentList = getDocumentsFromBucket(hentForsendelseResponse);
        exchange.setProperty(PROPERTY_ANTALL_DOK, dokdistDokumentList.size());
        final JournalpostQdist013 journalpostQdist013 = safJournalpostQueryService.hentJournalpost(hentForsendelseResponse.getArkivInformasjon()
                .getArkivId());
        final JAXBElement<Arkivmelding> arkivmeldingJAXBElement = avtaltmeldingMapper.createArkivMelding(journalpostQdist013, bestillingsId);
        final String arkivmeldingXmlString = marshalArkivmeldingToXmlString(arkivmeldingJAXBElement);

        log.info("Sender eformidling forsendelse direkte til Altinn formidlingstjenesten. forsendelseId={}, konversasjonsId={}, bestillingsId={}",
                forsendelseId, conversationId, bestillingsId);

        eformidling.send(NavDokumentpakke.builder()
                .conversationId(conversationId)
                .bestillingsId(bestillingsId)
                .messageChannelInstanceIdentifier(UUID.randomUUID())
                .arkivmelding(fromAvtaltmelding(new ByteArrayInputStream(arkivmeldingXmlString.getBytes(StandardCharsets.UTF_8))))
                .navDokumenter(dokdistDokumentList.stream()
                        .map(d -> fromVedlegg(getDocumentFilename(arkivmeldingJAXBElement.getValue(), journalpostQdist013.getJournalpostId(), d.getDokumentInfoId()),
                                new ByteArrayInputStream(d.getPdf())))
                        .collect(Collectors.toList()))
                .build(), arkivmeldingXmlString);

        juridiskLogg.lagreJuridiskLogg(lagreJuridiskLoggMapper.map(hentForsendelseResponse, arkivmeldingXmlString.getBytes()));
    }

    private List<DokdistDokument> getDocumentsFromBucket(HentForsendelseResponse hentForsendelseResponse) {
        return hentForsendelseResponse.getDokumenter().stream()
                .map(dokumentTo -> {
                    String jsonPayload = bucketStorage.downloadObject(dokumentTo.getDokumentObjektReferanse(), hentForsendelseResponse.getBestillingsId());
                    DokdistDokument dokdistDokument = deserializeBucketJsonPayloadToDokdistDokument(jsonPayload, dokumentTo.getDokumentObjektReferanse());
                    dokdistDokument.setDokumentInfoId(dokumentTo.getArkivDokumentInfoId());
                    return dokdistDokument;
                })
                .collect(Collectors.toList());
    }

    private String marshalArkivmeldingToXmlString(JAXBElement<Arkivmelding> arkivmeldingJAXBElement) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Arkivmelding.class);
            Marshaller marshaller = jaxbContext.createMarshaller();

            StringWriter sw = new StringWriter();
            marshaller.marshal(arkivmeldingJAXBElement, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new KunneIkkeMarshalleArkivmeldingTechnicalException("Kunne ikke marshalle Arkivmelding til xmlString", e);
        }
    }

    private String getDocumentFilename(Arkivmelding arkivmelding, String journalpostId, String dokumentInfoId) {
        Dokumentbeskrivelse dokumentbeskrivelse = getDokumentbeskrivelseByJpIdAndDokInfoId(arkivmelding, journalpostId, dokumentInfoId);
        return dokumentbeskrivelse.getDokumentobjekt().getFirst().getReferanseDokumentfil();

    }

    private Dokumentbeskrivelse getDokumentbeskrivelseByJpIdAndDokInfoId(Arkivmelding arkivmelding, String journalpostId, String dokumentInfoId) {
        Journalpost journalpost = (Journalpost) arkivmelding.getMappe().getFirst().getRegistrering().getFirst();
        return journalpost.getDokumentbeskrivelse().stream()
                .filter(dokumentbeskrivelse -> dokumentbeskrivelse.getDokumentobjekt()
                        .getFirst().getReferanseDokumentfil().startsWith(format("%s-%s", journalpostId, dokumentInfoId)))
                .findAny()
                .orElseThrow(() -> new IkkeSammenfallendeIderFunctionalException(format("DokumentInfoId=%s finnes på foresendelsen i dokdistDb, men ikke i respons fra SAF på journalpostId=%s.", dokumentInfoId, journalpostId)));
    }

    private static void validateThatForsendelseStatusIsKlarForDist(String forsendelseStatus) {
        if (!FORSENDELSE_STATUS_KLAR_FOR_DIST.equals(forsendelseStatus)) {
            throw new InvalidForsendelseStatusFunctionalException(
                    format("ForsendelseStatus må være %s. Fant forsendelseStatus=%s", FORSENDELSE_STATUS_KLAR_FOR_DIST, forsendelseStatus)
            );
        }
    }

    private static DokdistDokument deserializeBucketJsonPayloadToDokdistDokument(String jsonPayload, String objektReferanse) {
        try {
            DokdistDokument dokdistDokument = JsonSerializer.deserialize(jsonPayload, DokdistDokument.class);
            dokdistDokument.setDokumentObjektReferanse(objektReferanse);
            return dokdistDokument;
        } catch (IllegalStateException e) {
            throw new KunneIkkeDeserialisereBucketJsonPayloadFunctionalException(format("Kunne ikke deserialisere jsonPayload fra bucket for dokument med dokumentobjektreferanse=%s. Dokumentet er ikke persistert til bucket med korrekt format!", objektReferanse));
        }
    }
}
