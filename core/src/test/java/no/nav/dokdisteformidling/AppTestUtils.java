package no.nav.dokdisteformidling;

import static java.nio.charset.StandardCharsets.UTF_8;

import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public final class AppTestUtils {

	private AppTestUtils() {
	}

	public static String classpathToString(String classpathResource) throws IOException {
		try (InputStream inputStream = new ClassPathResource(classpathResource).getInputStream()) {
			return IOUtils.toString(inputStream, UTF_8);
		}
	}

	public static List<ZipFile> zipEntries(InputStream inputStream) {
		final List<ZipFile> zipEntries = new ArrayList<>();
		try {
			try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
				ZipEntry zipEntry;
				while ((zipEntry = zipInputStream.getNextEntry()) != null) {
					final byte[] contents = IOUtils.toByteArray(zipInputStream);
					zipEntries.add(new ZipFile(zipEntry.getName(), contents));
					zipInputStream.closeEntry();
				}
			}
			return zipEntries;
		} catch (IOException e) {
			return zipEntries;
		}
	}

	@Data
	public static class ZipFile {
		private final String name;
		private final byte[] contents;

		public String getContentsAsString() {
			return new String(contents, UTF_8);
		}
	}

	public static List<String> zipFilenames(InputStream inputStream) {
		return zipEntries(inputStream).stream().map(ZipFile::getName).collect(Collectors.toList());
	}
}
