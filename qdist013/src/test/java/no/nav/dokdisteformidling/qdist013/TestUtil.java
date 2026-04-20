package no.nav.dokdisteformidling.qdist013;

import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class TestUtil {

    private TestUtil() {
    }

    public static LocalDateTime convertFromXmlGregorianCalendarToLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
    }

    @SneakyThrows
    public static String classpathToString(String classpathResource) {
        try (InputStream inputStream = new ClassPathResource(classpathResource).getInputStream()) {
            return new String(inputStream.readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new IOException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
        }
    }

}
