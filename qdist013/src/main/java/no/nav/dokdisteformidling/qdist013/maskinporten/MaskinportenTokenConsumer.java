package no.nav.dokdisteformidling.qdist013.maskinporten;


import static no.nav.dokdisteformidling.constants.DomainConstants.DEFAULT_ZONE_ID;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.certificate.AppCertificate;
import no.nav.dokdisteformidling.config.props.MaskinportenProperties;
import no.nav.dokdisteformidling.metrics.Monitor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class MaskinportenTokenConsumer {
	// TODO nytt scope når virksomhetssertifikat er klart.
	private static final String SCOPE_DPO = "move/dpo.read";

	private final MaskinportenProperties props;
	private final RestTemplate restTemplate;

	public MaskinportenTokenConsumer(MaskinportenProperties props, RestTemplateBuilder restTemplateBuilder) {
		this.props = props;
		this.restTemplate = restTemplateBuilder
				.messageConverters(new FormHttpMessageConverter(),
						new MappingJackson2HttpMessageConverter())
				.errorHandler(new OidcErrorHandler())
				.setReadTimeout(Duration.ofSeconds(30))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
	}

	@Retryable(value = HttpClientErrorException.class, maxAttempts = 3, backoff = @Backoff(delay = 5000, multiplier = 3))
	@Monitor(value = "dok_consumer", extraTags = {"process", "maskinporten_fetchtoken"}, percentiles = {0.5, 0.95}, histogram = true)
	public OidcTokenResponse fetchToken() {
		LinkedMultiValueMap<String, String> attrMap = new LinkedMultiValueMap<>();
		attrMap.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
		attrMap.add("assertion", generateJWT());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(attrMap, headers);

		URI accessTokenUri;
		try {
			accessTokenUri = props.getUrl().toURI();
		} catch (URISyntaxException e) {
			log.error("Error converting property to URI", e);
			throw new RuntimeException(e);
		}

		log.info("Henter accessToken fra maskinporten på url={}", props.getUrl().toString());
		ResponseEntity<OidcTokenResponse> response = restTemplate.exchange(accessTokenUri, HttpMethod.POST,
				httpEntity, OidcTokenResponse.class);
		log.info("AccessToken hentet OK fra maskinporten på url={}", props.getUrl().toString());
		return response.getBody();
	}

	private String generateJWT() {
		AppCertificate nokkel = new AppCertificate(props.getKeystore());

		List<Base64> certChain = new ArrayList<>();
		try {
			certChain.add(Base64.encode(nokkel.getX509Certificate().getEncoded()));
		} catch (CertificateEncodingException e) {
			log.error("Could not get encoded certificate", e);
			throw new RuntimeException(e);
		}

		JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).x509CertChain(certChain).build();

		String clientId = props.getClientid();
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.audience(props.getAudience())
				.issuer(clientId)
				.claim("scope", getCurrentScopes())
				.jwtID(UUID.randomUUID().toString())
				.issueTime(Date.from(OffsetDateTime.now(DEFAULT_ZONE_ID).toInstant()))
				.expirationTime(Date.from(OffsetDateTime.now(DEFAULT_ZONE_ID).toInstant().plusSeconds(120)))
				.build();

		RSASSASigner signer = new RSASSASigner(nokkel.loadPrivateKey());

		if (nokkel.shouldLockProvider()) {
			signer.getJCAContext().setProvider(nokkel.getKeyStore().getProvider());
		}

		SignedJWT signedJWT = new SignedJWT(jwsHeader, claims);
		try {
			signedJWT.sign(signer);
		} catch (JOSEException e) {
			log.error("Error occured during signing of JWT", e);
		}

		return signedJWT.serialize();
	}

	public String getCurrentScopes() {
		ArrayList<String> scopeList = new ArrayList<>();
		scopeList.add(SCOPE_DPO);
		return scopeList.stream().reduce((a, b) -> a + " " + b).orElse("");
	}
}
