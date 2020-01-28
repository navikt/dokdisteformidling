package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.inject.Named;

/**
 * Endret og tilpasset for NAV sin bruk fra https://github.com/difi/move-integrasjonspunkt
 *
 * Egen config for skriving av sbd.json
 *
 * @see EformidlingMessagePackager
 */
@Configuration
public class JacksonConfig {
	@Bean
	@Named("eformidlingObjectMapper")
	public ObjectMapper eformidlingObjectMapper() {
		return new Jackson2ObjectMapperBuilder()
				.modulesToInstall(new JavaTimeModule())
				.serializationInclusion(JsonInclude.Include.NON_NULL)
				.featuresToEnable(
						SerializationFeature.INDENT_OUTPUT,
						JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
						MapperFeature.DEFAULT_VIEW_INCLUSION)
				.featuresToDisable(
						SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
						SerializationFeature.CLOSE_CLOSEABLE,
						JsonGenerator.Feature.AUTO_CLOSE_TARGET).build();
	}
}
