package no.nav.dokdisteformidling.consumer.token;

import no.nav.dokdisteformidling.config.props.NaisProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Component
public class NaisTexasConsumer {

	private static final Pattern TARGET_PATTERN = Pattern.compile("api://[^.]+\\.[^.]+\\.[^.]+/\\.default");
	private static final String ENTRA_ID = "entra_id";
	private final RestClient restClient;

	public NaisTexasConsumer(RestClient.Builder restClientBuilder, NaisProperties naisProperties) {
		this.restClient = restClientBuilder
				.baseUrl(naisProperties.tokenEndpoint())
				.build();
	}

	/// Maskin-til-maskin systemtoken fra Texas
	///
	/// @param targetScope Maskin man vil autorisere mot på format `api://<cluster>.<namespace>.<other-api-app-name>/.default`
	/// @return entra_id utstedt maskin-til-maskin token
	public String getSystemToken(String targetScope) {
		if (isBlank(targetScope) || !TARGET_PATTERN.matcher(targetScope).matches()) {
			throw new IllegalArgumentException("Ugyldig targetScope. Må være på format api://<cluster>.<namespace>.<other-api-app-name>/.default");
		}

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", ENTRA_ID);
		formData.add("target", targetScope);

		return requireNonNull(restClient.post()
				.contentType(APPLICATION_FORM_URLENCODED)
				.body(formData)
				.retrieve()
				.body(NaisTexasToken.class))
				.accessToken();
	}

}