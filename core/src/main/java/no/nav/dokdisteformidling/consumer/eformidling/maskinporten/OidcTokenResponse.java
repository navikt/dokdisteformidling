package no.nav.dokdisteformidling.consumer.eformidling.maskinporten;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(SnakeCaseStrategy.class)
public class OidcTokenResponse {

    private String accessToken;
    private Integer expiresIn;
    private String scope;
}