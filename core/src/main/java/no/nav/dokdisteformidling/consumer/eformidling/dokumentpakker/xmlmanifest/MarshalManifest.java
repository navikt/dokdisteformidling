package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.OutputStream;

@Slf4j
@UtilityClass
final class MarshalManifest {
    static void marshal(Manifest doc, OutputStream os) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(new Class[]{Manifest.class}, null);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(doc, os);
        } catch (JAXBException e) {
            log.error("Marshalling failed", e);
        }
    }
}
