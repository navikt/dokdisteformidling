package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.xmlmanifest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentpakkingException;
import org.glassfish.jaxb.runtime.v2.runtime.IllegalAnnotationsException;

import java.io.OutputStream;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

@Slf4j
@UtilityClass
final class MarshalManifest {
	private static final JAXBContext JAXB_CONTEXT;

	static {
		try {
			// JAXBContext implementasjoner skal være trådsikre
			JAXB_CONTEXT = JAXBContext.newInstance(Manifest.class);
		} catch (JAXBException e) {
			throw new IllegalStateException("Klarte ikke sette opp JAXBContext", e);
		}
	}

	static void marshal(Manifest doc, OutputStream os) {
		try {
			Marshaller jaxbMarshaller = JAXB_CONTEXT.createMarshaller();
			jaxbMarshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(doc, os);
		} catch (JAXBException e) {
			if (e instanceof IllegalAnnotationsException illegalAnnotationsException) {
				illegalAnnotationsException.getErrors().forEach(
						error -> log.error(error.getMessage())
				);
			}
			throw new DokumentpakkingException("Klarte ikke å marshalle Manifest", e);
		}
	}
}
