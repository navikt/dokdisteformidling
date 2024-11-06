package no.nav.dokdisteformidling;

import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class AppTestUtils {

	private AppTestUtils() {
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
}
