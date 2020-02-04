package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions;

public class AltinnPackageException extends RuntimeException {

    public AltinnPackageException(Throwable e) {
        super(e);
    }

    public AltinnPackageException(String message) {
        super(message);
    }

    public AltinnPackageException(String s, Throwable e) {
        super(s, e);
    }

    public AltinnPackageException() {
        super();
    }
}
