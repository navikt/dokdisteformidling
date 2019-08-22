package no.nav.dokdisteformidling;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public final class testUtils {

	private testUtils() {
	}

	public static String classpathToString(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		String message = IOUtils.toString(inputStream, UTF_8);
		IOUtils.closeQuietly(inputStream);
		return message;
	}

	public static String fileToString(File file) throws IOException {
		byte[] data = new byte[(int) file.length()];
		try (FileInputStream fis = new FileInputStream(file)) {
			fis.read(data);
		}
		return new String(data);
	}

}
