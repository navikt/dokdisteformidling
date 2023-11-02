package no.nav.dokdisteformidling.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.apache.cxf.phase.Phase.PRE_PROTOCOL;

/**
 * Kopiert fra https://github.com/Altinn/ec-client-java-cxf
 *
 * Interceptor for å hente <i>Cookie</i> fra {@link CookieStore} og legge til i header i utgående webservice melding.
 */
@Slf4j
public class CookiesOutInterceptor extends AbstractPhaseInterceptor {
    public CookiesOutInterceptor() {
        super(PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) {
        Map<String, List> headers = (Map<String, List>) message.get(PROTOCOL_HEADERS);
        if (CookieStore.getCookie() != null) {
            headers.put("Cookie", Collections.singletonList(CookieStore.getCookie()));
        }
    }

}