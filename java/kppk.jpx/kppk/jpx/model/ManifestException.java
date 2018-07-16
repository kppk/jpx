package kppk.jpx.model;

/**
 * TODO: Document this
 */
public final class ManifestException extends RuntimeException {

    public ManifestException(String message) {
        super(message);
    }

    public ManifestException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManifestException(Throwable cause) {
        super(cause);
    }

    protected ManifestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
