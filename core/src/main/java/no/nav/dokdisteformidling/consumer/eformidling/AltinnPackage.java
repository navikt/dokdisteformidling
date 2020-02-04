package no.nav.dokdisteformidling.consumer.eformidling;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import no.altinn.schema.services.serviceengine.broker._2015._06.BrokerServiceManifest;
import no.altinn.schema.services.serviceengine.broker._2015._06.BrokerServiceRecipientList;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.ArkivmeldingKvitteringMessage;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.MessagePersister;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.AltinnPackageException;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh.StandardBusinessDocument;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest.Payload;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.springframework.context.ApplicationContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.EFORMIDLING_ASIC;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.EFORMIDLING_SBD;


@Builder
@Value
@Slf4j
public class AltinnPackage {
    private static final String CONTENT_XML = "content.xml";
    private static final String RECIPIENTS_XML = "recipients.xml";
    private static final String MANIFEST_XML = "manifest.xml";

    private static JAXBContext ctx;
    private final BrokerServiceManifest manifest;
    private final BrokerServiceRecipientList recipient;
    private final StandardBusinessDocument sbd;
    private final InputStream asicInputStream;

    static {
        try {
            ctx = JAXBContextFactory.create(new Clkass[]{BrokerServiceManifest.class,
                    BrokerServiceRecipientList.class, StandardBusinessDocument.class, Payload.class, ArkivmeldingKvitteringMessage.class}, new HashMap());
        } catch (JAXBException e) {
            throw new AltinnPackageException("Could not create JAXBContext", e);
        }
    }

    private AltinnPackage(BrokerServiceManifest manifest,
                          BrokerServiceRecipientList recipient,
                          StandardBusinessDocument sbd,
                          InputStream asicInputStream) {
        this.manifest = manifest;
        this.recipient = recipient;
        this.sbd = sbd;
        this.asicInputStream = asicInputStream;
    }

    public static AltinnPackage from(File f, MessagePersister messagePersister, ApplicationContext context) throws IOException, JAXBException {
        try (ZipFile zipFile = new ZipFile(f)) {
            Unmarshaller unmarshaller = ctx.createUnmarshaller();

            BrokerServiceManifest manifest = null;
            BrokerServiceRecipientList recipientList = null;
            StandardBusinessDocument sbd = null;
            InputStream asicInputStream = null;
            long asicSize = 0;

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                switch (zipEntry.getName()) {
                    case MANIFEST_XML:
                        manifest = (BrokerServiceManifest) unmarshaller.unmarshal(zipFile.getInputStream(zipEntry));
                        break;
                    case RECIPIENTS_XML:
                        recipientList = (BrokerServiceRecipientList) unmarshaller.unmarshal(zipFile.getInputStream(zipEntry));
                        break;
                    case EFORMIDLING_SBD:
                        ObjectMapper om = context.getBean(ObjectMapper.class);
                        om.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
                        sbd = om.readValue(zipFile.getInputStream(zipEntry), StandardBusinessDocument.class);
                        break;
                    case CONTENT_XML:
                        StreamSource source = new StreamSource(zipFile.getInputStream(zipEntry));
                        sbd = unmarshaller.unmarshal(source, StandardBusinessDocument.class).getValue();
                        break;
                    case EFORMIDLING_ASIC:
                        asicInputStream = zipFile.getInputStream(zipEntry);
                        asicSize = zipEntry.getSize();
                        break;
                    default:
                        log.info("Hopper over fil: {}", zipEntry.getName());
                }
            }

            if (sbd == null) {
                throw new AltinnPackageException("Altinn zip inneholder ikke Trygderett arkivmeldingKvittering, kan ikke fortsette. ");
            }
            if (asicInputStream != null) {
                messagePersister.writeStream(sbd.getDocumentId(), EFORMIDLING_ASIC, asicInputStream, asicSize);
            }
            return new AltinnPackage(manifest, recipientList, sbd, null);
        }
    }
}
