package no.nav.dokdisteformidling.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoCloseableTempFile implements AutoCloseable {

    private final Path tempFile;

    public AutoCloseableTempFile(String prefix, String suffix) throws IOException {
        tempFile = Files.createTempFile(prefix, suffix);
    }

    public Path getTempFile() {
        return tempFile;
    }

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    public File toFile() {
        return tempFile.toFile();
    }
}
