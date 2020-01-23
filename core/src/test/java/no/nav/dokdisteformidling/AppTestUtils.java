package no.nav.dokdisteformidling;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public final class AppTestUtils {

	private AppTestUtils() {
	}

	public static String classpathToString(String classpathResource) throws IOException {
		try(InputStream inputStream = new ClassPathResource(classpathResource).getInputStream()) {
			return IOUtils.toString(inputStream, UTF_8);
		}
	}

	public static List<String> zipFilenames(InputStream inputStream) throws Exception {
		final List<String> filenames = new ArrayList<>();
		try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
			ZipEntry zipEntry;
			while ((zipEntry = zipInputStream.getNextEntry()) != null) {
				filenames.add(zipEntry.getName());
			}
		}
		return filenames;
	}
}
