package no.nav.dokdisteformidling.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.interceptor.Soap12FaultInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.TokenStoreUtils;

import javax.xml.namespace.QName;
import java.util.List;

@Slf4j
@SuppressWarnings("rawtypes")
public class BadTokenInFaultInterceptor extends AbstractPhaseInterceptor {

    private static final String ERROR_CODE_BAD_CONTEXT_TOKEN = "BadContextToken";

    @SuppressWarnings("unchecked")
    public BadTokenInFaultInterceptor() {
        super(Phase.UNMARSHAL);
        getAfter().add(Soap12FaultInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Exception exception = message.getContent(Exception.class);
        if (exception instanceof SoapFault) {
            log.error("Det feilet, skal sende soapfault ..");
            SoapFault soapFault = (SoapFault) exception;
            List<QName> faultSubCodes = soapFault.getSubCodes();
            faultSubCodes.stream()
                    .forEach(subCode -> {
                        log.error("Fant soapfault subCode: {}", subCode.getLocalPart());
                        if (subCode.getLocalPart().equalsIgnoreCase(ERROR_CODE_BAD_CONTEXT_TOKEN)) {
                            String tokenId = (String) message.getContextualProperty(SecurityConstants.TOKEN_ID);
                            sletteTokenFramMessageAndTokenStore(message, tokenId);
                            CookieStore.setCookie(null);
                            soapFault.setMessage(String.format("Token med tokenId:%s har fjernet fra tokenstore, og nye token skal be om i neste kall", tokenId));
                            message.setContent(Exception.class, soapFault);
                        }

                    });
        }

    }

    private void sletteTokenFramMessageAndTokenStore(Message message, String tokenId) {
        message.getExchange().getEndpoint().remove(SecurityConstants.TOKEN);
        message.getExchange().getEndpoint().remove(SecurityConstants.TOKEN_ID);
        message.getExchange().remove(SecurityConstants.TOKEN_ID);
        message.getExchange().remove(SecurityConstants.TOKEN);
        TokenStoreUtils.getTokenStore(message).remove(tokenId);
        log.error("Token med tokenId: {} har fjernet fra meldingen og tokenStore ", tokenId);
    }
}
