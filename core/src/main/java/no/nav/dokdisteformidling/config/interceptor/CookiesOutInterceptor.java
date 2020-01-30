package no.nav.dokdisteformidling.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Kopiert fra https://github.com/Altinn/ec-client-java-cxf
 *
 * Interceptor for å hente <i>Cookie</i> fra {@link CookieStore} og legge til i header i utgående webservice melding.
 */
@Slf4j
public class CookiesOutInterceptor extends AbstractPhaseInterceptor {
    public CookiesOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Map<String, List> headers = (Map<String, List>) message.get(Message.PROTOCOL_HEADERS);
        if (CookieStore.getCookie() != null) {
            log.info("CookiesOUTInterceptor -- cookie to attach to header: " + CookieStore.getCookie());
            headers.put("Cookie", Collections.singletonList(CookieStore.getCookie()));
        }
    }

}