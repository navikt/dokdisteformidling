package no.nav.dokdisteformidling.utils;

import org.springframework.util.Assert;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;

public class XmlUtils {

    private XmlUtils() {
    }

    public static <T> T unmarshalXmlObject(InputStream inputStream, Class<T> tClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(tClass);
        Unmarshaller unmarshal = context.createUnmarshaller();
        Object object = unmarshal.unmarshal(inputStream);
        Assert.isInstanceOf(tClass, object);
        return (T) object;
    }
}
