package no.nav.dokdisteformidling.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Kopiert fra https://github.com/Altinn/ec-client-java-cxf
 *
 * Interceptor for å legge til attributten <i>Connection</i> med verdi <i>Keep-Alive</i>
 * i header på utgående webservice melding.
 */
@Slf4j
public class HeaderOutInterceptor extends AbstractPhaseInterceptor {
    public HeaderOutInterceptor() {
        super(Phase.PRE_PROTOCOL_ENDING);
        getAfter().add(SAAJOutInterceptor.SAAJOutEndingInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) {
        log.info("Adding Keep-Alive header");
        Map<String, List> headers = (Map<String, List>) message.get(Message.PROTOCOL_HEADERS);
        headers.put("Connection", Collections.singletonList("Keep-Alive"));
    }

}