package no.nav.dokdisteformidling.config.interceptor;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

public class ClientCallBackHandler implements CallbackHandler {

    private final String password;

    public ClientCallBackHandler(String password) {
        this.***passord=gammelt_passord***;
    }

    @Override
    public void handle(Callback[] callbacks) {

        WS***passord=gammelt_passord***];
        wsPasswordCallback.setPassword(password);

    }
}
