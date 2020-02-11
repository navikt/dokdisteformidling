package no.nav.dokdisteformidling.qdist013;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public final class TestUtil {

    private TestUtil() {
    }

    public static LocalDateTime convertFromXmlGregorianCalendarToLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
    }

    @SneakyThrows
    public static String classpathToString(String classpathResource) {
        try {
            InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
            String message = IOUtils.toString(inputStream, UTF_8);
            IOUtils.closeQuietly(inputStream);
            return message;
        } catch (IOException e) {
            throw new IOException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
        }
    }


}
