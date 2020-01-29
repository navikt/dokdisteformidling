package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AltinnReason {

    private final Integer id;

    private final String message;
    private final String userId;
    private final String localized;

    @Override
    public String toString() {
        return String.format("Reason: %s. LocalizedErrorMessage: %s. ErrorId: %d. UserId: %s", message, localized, id, userId);
    }
}
