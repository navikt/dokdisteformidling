package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.config.props.ServiceRegistryProperties;
import no.nav.dokdisteformidling.consumer.eformidling.maskinporten.MaskinportenTechnicalException;
import no.nav.dokdisteformidling.consumer.eformidling.maskinporten.MaskinportenTokenConsumer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;

/**
 * Konsument mot Difi Service Registry (SR)
 */
@Slf4j
@Component
public class ServiceRegistryConsumer {

    public static final String OIDC_AUTHORIZATION_PREFIX = "Bearer ";
    public static final String TEKNISK_FEIL_ERROR_MESSAGE = "Klarte ikke hente mottakerInfo fra service registry. Teknisk feil: ";
    public static final String FUNKSJONELL_FEIL_ERROR_MESSAGE = "Klarte ikke hente mottakerInfo fra service registry. Funksjonell feil: ";
    private final MaskinportenTokenConsumer maskinportenTokenConsumer;
    private final RestTemplate restTemplate;
    private final String baseUrl;

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

    @Retryable(retryFor = {MaskinportenTechnicalException.class, ServiceRegistryTechnicalException.class})
    public IdentifierResource getIdentifierResource(final String orgnummer, final String serviceProcess) {
        final String accessToken = maskinportenTokenConsumer.fetchToken().getAccessToken();

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("identifier/" + orgnummer + "/process/" + serviceProcess)
                .build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.put(AUTHORIZATION, singletonList(OIDC_AUTHORIZATION_PREFIX + accessToken));
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        try {
            final ResponseEntity<IdentifierResource> exchange = restTemplate.exchange(uri, GET, httpEntity, IdentifierResource.class);
            return exchange.getBody();
        } catch (HttpClientErrorException e) {
            log.warn(FUNKSJONELL_FEIL_ERROR_MESSAGE + e.getResponseBodyAsString(), e);
            return IdentifierResource.empty();
        } catch (HttpServerErrorException e) {
            final String errorMessage = TEKNISK_FEIL_ERROR_MESSAGE + e.getResponseBodyAsString();
            log.error(errorMessage, e);
            throw new ServiceRegistryTechnicalException(errorMessage, e);
        }
    }
}
