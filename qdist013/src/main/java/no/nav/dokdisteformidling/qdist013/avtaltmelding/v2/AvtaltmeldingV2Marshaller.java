package no.nav.dokdisteformidling.qdist013.avtaltmelding.v2;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;
import no.arkivverket.standarder.noark5.arkivmelding.v2.Arkivmelding;
import no.nav.avtaltmelding.trygderetten.v1.NavMappe;
import no.nav.dokdisteformidling.exception.technical.KunneIkkeMarshalleArkivmeldingTechnicalException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.Map;

@Profile("avtaltmelding_v2")
@Component
public class AvtaltmeldingV2Marshaller {
	private final JAXBContext jaxbContext;
	private final Map<String, Object> marshalProperties;

	public AvtaltmeldingV2Marshaller() {
		this(Map.of());
	}

	public AvtaltmeldingV2Marshaller(Map<String, Object> marshalProperties) {
		try {
			this.jaxbContext = JAXBContext.newInstance(Arkivmelding.class, NavMappe.class);
			this.marshalProperties = marshalProperties;
		} catch (JAXBException e) {
			throw new IllegalStateException("Kunne ikke opprette JAXBContext", e);
		}
	}

	public String marshal(JAXBElement<Arkivmelding> arkivmeldingJAXBElement) {
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			if (!marshalProperties.isEmpty()) {
				configureMarshaller(marshaller);
			}

			StringWriter sw = new StringWriter();
			marshaller.marshal(arkivmeldingJAXBElement, sw);
			return sw.toString();
		} catch (JAXBException e) {
			throw new KunneIkkeMarshalleArkivmeldingTechnicalException("Kunne ikke marshalle Arkivmelding til xmlString", e);
		}
	}

	private void configureMarshaller(Marshaller marshaller) {
		marshalProperties.forEach((key, value) -> {
			try {
				marshaller.setProperty(key, value);
			} catch (PropertyException e) {
				throw new IllegalStateException("Kunne ikke konfigurere Marshaller", e);
			}
		});
	}
}
