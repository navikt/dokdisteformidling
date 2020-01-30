package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.mapper;

import no.altinn.brokerserviceexternal.ArrayOfFile;
import no.altinn.brokerserviceexternal.File;
import no.altinn.brokerserviceexternal.ObjectFactory;

public class FileListBuilder {

    String filename;

    public FileListBuilder withFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public javax.xml.bind.JAXBElement<ArrayOfFile> build() {
        ObjectFactory objectFactory = new ObjectFactory();
        ArrayOfFile arrayOfFile = new ArrayOfFile();
        no.altinn.brokerserviceexternal.File file = new File();
        file.setFileName(filename);
        arrayOfFile.getFile().add(file);
        return objectFactory.createArrayOfFile(arrayOfFile);
    }
}
