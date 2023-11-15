package no.nav.dokdisteformidling.consumer.eformidling.maskinporten;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

@Slf4j
public class OidcErrorHandler extends DefaultResponseErrorHandler {

    @Override
    protected void handleError(ClientHttpResponse response, HttpStatusCode statusCode) throws IOException {
        if (statusCode.is4xxClientError()) {
            throw new HttpClientErrorException(statusCode, response.getStatusText(),
                    response.getHeaders(), getResponseBody(response), getCharset(response));
        } else if (statusCode.is5xxServerError()) {
            throw new HttpServerErrorException(statusCode, response.getStatusText(),
                    response.getHeaders(), getResponseBody(response), getCharset(response));
        }
        throw new RestClientException("Unknown status code [" + statusCode + "]");
    }
}
