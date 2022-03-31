package no.nav.dokdisteformidling.utils;

import no.nav.dokdisteformidling.constants.DomainConstants;
import no.nav.dokdisteformidling.exception.functional.InvalidForsendelseStatusFunctionalException;
import no.nav.dokdisteformidling.exception.functional.KunneIkkeDeserialisereBucketJsonPayloadFunctionalException;
import no.nav.dokdisteformidling.storage.DokdistDokument;
import no.nav.dokdisteformidling.storage.JsonSerializer;

import java.util.UUID;

import static java.lang.String.format;

/**
 * @author Heidi Elisabeth Sando, Visma Consulting.
 */

public final class FunctionalUtils {

    private FunctionalUtils() {
    }

    public static void validateThatForsendelseStatusIsKlarForDist(String forsendelseStatus) {
        if (!DomainConstants.FORSENDELSE_STATUS_KLAR_FOR_DIST.equals(forsendelseStatus)) {
            throw new InvalidForsendelseStatusFunctionalException(String.format("ForsendelseStatus må være %s. Fant forsendelseStatus=%s",
                    DomainConstants.FORSENDELSE_STATUS_KLAR_FOR_DIST, forsendelseStatus));
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
        return UUID.randomUUID().toString();
    }

}