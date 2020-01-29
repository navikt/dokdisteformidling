package no.nav.dokdisteformidling.config.interceptor;

/**
 * Kopiert fra https://github.com/Altinn/ec-client-java-cxf
 */
public class CookieStore {

    private static ThreadLocal<Object> requestCookie= new ThreadLocal<>();

    public CookieStore() {
        throw new AssertionError("Instantiating cookies class");
    }

    public static void setCookie(Object cookie) {
        requestCookie.set(cookie);
    }

    public static Object getCookie(){
        return requestCookie.get();
    }
}
