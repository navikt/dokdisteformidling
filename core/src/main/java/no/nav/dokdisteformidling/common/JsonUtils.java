package no.nav.dokdisteformidling.common;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import lombok.var;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;

public class JsonUtils {

    private JsonUtils() {
    }

    private static final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        var om = new ObjectMapper();
        om.registerModule(new JavaTimeModule().addDeserializer(OffsetDateTime.class, InstantDeserializer.OFFSET_DATE_TIME));
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om;
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static <T> T toObject(InputStream jsonPayload, Class<T> type) {
        try {
            return objectMapper.readValue(jsonPayload, type);
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid json ", e);
        }
    }

}
