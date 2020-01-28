package no.nav.dokdisteformidling.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.Cookie;

import java.util.List;
import java.util.Map;

/**
 * Intercept cookies fra header i innkommende webservice melding.
 */
@Slf4j
@SuppressWarnings("rawtypes")
public class CookiesInInterceptor  extends AbstractPhaseInterceptor {

    public CookiesInInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Map<String, List> headers = (Map<String, List>) message.get(Message.PROTOCOL_HEADERS);
        List<Cookie> cookies = headers.get("Set-Cookie");
        if(cookies != null){
            log.debug("CookiesInInterceptor -- cookies kan bli lagret i cookiestore:{}" + cookies.get(0));
        }
    }
}
