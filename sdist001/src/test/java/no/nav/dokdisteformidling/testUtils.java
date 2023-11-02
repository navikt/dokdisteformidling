package no.nav.dokdisteformidling;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class testUtils {

    private testUtils() {
    }

    @SneakyThrows
    public static String classpathToString(String classpathResource) {
        try (InputStream inputStream = new ClassPathResource(classpathResource).getInputStream()) {
            return IOUtils.toString(inputStream, UTF_8);
        } catch (IOException e) {
            throw new IOException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
        }
    }

    @SneakyThrows
    public static byte[] classpathToByteArray(String classpathResource) {
        try (InputStream content = new ClassPathResource(classpathResource).getInputStream()) {
            ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
            IOUtils.copy(content, zipFile);
            return zipFile.toByteArray();
        } catch (IOException e) {
            throw new IOException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
        }
    }
}
