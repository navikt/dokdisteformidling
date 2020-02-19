package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.config.props.ServiceRegistryProperties;
import no.nav.dokdisteformidling.consumer.eformidling.maskinporten.MaskinportenTechnicalException;
import no.nav.dokdisteformidling.consumer.eformidling.maskinporten.MaskinportenTokenConsumer;
import no.nav.dokdisteformidling.metrics.Monitor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;

/**
 * Konsument mot Difi Service Registry (SR)
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class ServiceRegistryConsumer {
    public static final String OIDC_AUTHORIZATION_PREFIX = "Bearer ";
    public static final String TEKNISK_FEIL_ERROR_MESSAGE = "Klarte ikke hente mottakerInfo fra service registry. Teknisk feil.";
    public static final String FUNKSJONELL_FEIL_ERROR_MESSAGE = "Klarte ikke hente mottakerInfo fra service registry. Funksjonell feil.";
    private final MaskinportenTokenConsumer maskinportenTokenConsumer;
    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Inject
    public ServiceRegistryConsumer(ServiceRegistryProperties serviceRegistryProperties,
                                   MaskinportenTokenConsumer maskinportenTokenConsumer,
                                   RestTemplateBuilder restTemplateBuilder) {
        this.maskinportenTokenConsumer = maskinportenTokenConsumer;
        this.restTemplate = restTemplateBuilder
                .setReadTimeout(Duration.ofSeconds(30))
                .setConnectTimeout(Duration.ofSeconds(5))
                .build();
        this.baseUrl = serviceRegistryProperties.getUrl().toString();
    }

    @Retryable(value = {MaskinportenTechnicalException.class, ServiceRegistryTechnicalException.class}, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    @Monitor(value = "dok_consumer", extraTags = {"process", "serviceregistry"}, percentiles = {0.5, 0.95}, histogram = true)
    public IdentifierResource getIdentifierResource(final String orgnummer, final String serviceProcess) {
        final String accessToken = maskinportenTokenConsumer.fetchToken().getAccessToken();

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("identifier/" + orgnummer + "/process/" + serviceProcess)
                .build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.AUTHORIZATION, Collections.singletonList(OIDC_AUTHORIZATION_PREFIX + accessToken));
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        try {
            final ResponseEntity<IdentifierResource> exchange = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, IdentifierResource.class);
            return exchange.getBody();
        } catch (HttpClientErrorException e) {
            log.warn(FUNKSJONELL_FEIL_ERROR_MESSAGE, e);
            return IdentifierResource.empty();
        } catch (HttpServerErrorException e) {
            log.error(TEKNISK_FEIL_ERROR_MESSAGE, e);
            throw new ServiceRegistryTechnicalException(TEKNISK_FEIL_ERROR_MESSAGE, e);
        }
    }
}
