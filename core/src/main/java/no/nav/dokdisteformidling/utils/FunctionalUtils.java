package no.nav.dokdisteformidling.utils;

import no.nav.dokdisteformidling.exception.functional.InvalidForsendelseStatusFunctionalException;
import no.nav.dokdisteformidling.exception.functional.KunneIkkeDeserialisereBucketJsonPayloadFunctionalException;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static no.nav.dokdisteformidling.constants.DomainConstants.FORSENDELSE_STATUS_KLAR_FOR_DIST;

public final class FunctionalUtils {

    private FunctionalUtils() {
    }

    public static void validateThatForsendelseStatusIsKlarForDist(String forsendelseStatus) {
        if (!FORSENDELSE_STATUS_KLAR_FOR_DIST.equals(forsendelseStatus)) {
            throw new InvalidForsendelseStatusFunctionalException(
                    format("ForsendelseStatus må være %s. Fant forsendelseStatus=%s", FORSENDELSE_STATUS_KLAR_FOR_DIST, forsendelseStatus)
            );
        }
    }

    public static DokdistDokument deserializeBucketJsonPayloadToDokdistDokument(String jsonPayload, String objektReferanse) {
        DokdistDokument dokdistDokument;

        try {
            dokdistDokument = JsonSerializer.deserialize(jsonPayload, DokdistDokument.class);
            dokdistDokument.setDokumentObjektReferanse(objektReferanse);
        } catch (IllegalStateException e) {
            throw new KunneIkkeDeserialisereBucketJsonPayloadFunctionalException(format("Kunne ikke deserialisere jsonPayload fra bucket for dokument med dokumentobjektreferanse=%s. Dokumentet er ikke persistert til bucket med korrekt format!", objektReferanse));
        }

        return dokdistDokument;
    }

    public static String generateRandomUUID() {
        return randomUUID().toString();
    }

}