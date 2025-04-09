package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS;
import static com.fasterxml.jackson.databind.MapperFeature.DEFAULT_VIEW_INCLUSION;
import static com.fasterxml.jackson.databind.SerializationFeature.CLOSE_CLOSEABLE;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static no.nav.dokdisteformidling.constants.DomainConstants.DEFAULT_ZONE_ID;

/**
 * Endret og tilpasset for NAV sin bruk fra https://github.com/difi/move-integrasjonspunkt
 *
 * Egen config for skriving av sbd.json
 *
 * @see EformidlingMessagePackager
 */
@Configuration
public class JacksonConfig {

	@Bean("eformidlingObjectMapper")
	public ObjectMapper eformidlingObjectMapper(Clock clock) {
		return new Jackson2ObjectMapperBuilder()
				.deserializerByType(OffsetDateTime.class, new IsoDateTimeDeserializer(clock))
				.modulesToInstall(new JavaTimeModule())
				.serializationInclusion(NON_NULL)
				.featuresToEnable(
						INDENT_OUTPUT,
						ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(),
						DEFAULT_VIEW_INCLUSION)
				.featuresToDisable(
						WRITE_DATES_AS_TIMESTAMPS,
						CLOSE_CLOSEABLE,
						AUTO_CLOSE_TARGET).build();
	}

	private static final class IsoDateTimeDeserializer extends InstantDeserializer<OffsetDateTime> {

		IsoDateTimeDeserializer(Clock clock) {
			super(
					OffsetDateTime.class,
					ISO_DATE_TIME,
					temporal -> getOffsetDateTime(clock, temporal),
					a -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
					a -> OffsetDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
					(d, z) -> d.withOffsetSameInstant(z.getRules().getOffset(d.toLocalDateTime())),
					true // yes, replace +0000 with Z
			);
		}

		private static OffsetDateTime getOffsetDateTime(Clock clock, TemporalAccessor temporal) {
			ZoneId obj = temporal.query(TemporalQueries.zone());

			if (obj != null) {
				return OffsetDateTime.from(temporal);
			}

			return LocalDateTime.from(temporal)
					.atOffset(DEFAULT_ZONE_ID.getRules().getOffset(LocalDateTime.now(clock)));
		}
	}
}
