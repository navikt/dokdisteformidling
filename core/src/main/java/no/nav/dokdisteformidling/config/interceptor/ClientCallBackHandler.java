package no.nav.dokdisteformidling.config.interceptor;

import no.nav.dokdisteformidling.config.props.DpoUserProperties;
import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

public class ClientCallBackHandler implements CallbackHandler {

    private final DpoUserProperties dpoUserProperties;

    public ClientCallBackHandler(DpoUserProperties dpoUserProperties) {
        this.dpoUserProperties = dpoUserProperties;
    }

    @Override
    public void handle(Callback[] callbacks) {
        WSPasswordCallback wsPasswordCallback = (WSPasswordCallback) callbacks[0];
        wsPasswordCallback.setPassword(dpoUserProperties.password());
    }
}
