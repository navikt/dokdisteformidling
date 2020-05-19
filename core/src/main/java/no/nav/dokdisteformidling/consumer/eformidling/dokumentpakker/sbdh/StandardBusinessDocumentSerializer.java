package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.AvtaltMessage;

import java.io.IOException;

public class StandardBusinessDocumentSerializer extends StdSerializer<StandardBusinessDocument> {

    protected StandardBusinessDocumentSerializer() {
        super(StandardBusinessDocument.class);
    }

    @Override
    public void serialize(StandardBusinessDocument value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("standardBusinessDocumentHeader");
        gen.writeObject(value.getStandardBusinessDocumentHeader());
        if (value.getAny() instanceof AvtaltMessage) {
            gen.writeFieldName("avtalt");
        } else {
            throw new UnsupportedOperationException("Kun avtaltmelding er støttet.");
        }
        gen.writeObject(value.getAny());
        gen.writeEndObject();
    }
}
