package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.glassfish.jaxb.runtime.v2.runtime.IllegalAnnotationsException;

import java.io.OutputStream;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

@Slf4j
@UtilityClass
final class MarshalManifest {

    static void marshal(Manifest doc, OutputStream os) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(new Class[]{Manifest.class}, null);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(doc, os);
        } catch (JAXBException jaxbException) {
            log.warn("Marshalling failed", jaxbException);

            if (jaxbException instanceof IllegalAnnotationsException illegalAnnotationsException) {
                illegalAnnotationsException.getErrors().forEach(
                        e -> log.warn(e.getMessage())
                );
            }
        }
    }
}
