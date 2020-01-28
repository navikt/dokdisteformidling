package no.nav.dokdisteformidling.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

@Slf4j
public class HeaderInterceptor extends AbstractPhaseInterceptor {

    public HeaderInterceptor() {
        super(Phase.PRE_PROTOCOL_ENDING);
        getAfter().add(SAAJOutInterceptor.SAAJOutEndingInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) throws Fault {

    }
}
