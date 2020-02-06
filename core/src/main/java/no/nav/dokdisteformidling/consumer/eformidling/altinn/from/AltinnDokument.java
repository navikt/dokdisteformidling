package no.nav.dokdisteformidling.consumer.eformidling.altinn.from;

import lombok.Builder;
import lombok.Value;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.ArkivmeldingKvitteringMessage;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocumentHeader;

@Builder
@Value
public class AltinnDokument {
    public static final String ARKIVMELDINGKVITTERING_XML_FILENAME = "arkivmelding_kvittering.xml";
    public static final String STANDARDBUSINESSDOCUMENTHEADER = "standardBusinessDocumentHeader.xml";
    public static final String CONTENT_XML = "content.xml";

    private final String fileReferance;
    private final ArkivmeldingKvitteringMessage arkivmeldingKvitteringMessage;
    private final StandardBusinessDocumentHeader sbdh;
    private final StandardBusinessDocument sbd;
}
