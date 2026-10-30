package org.curljni;

public class CURLException extends RuntimeException {
    public final long code;
    public final String strError;
    public final String strErrorBuffer;
    public final long errno;
    public final String errnoMessage;

    public CURLException(long code, String strError, String strErrorBuffer, long errno, String errnoMessage) {
        this.code = code;
        this.strError = strError;
        this.strErrorBuffer = strErrorBuffer;
        this.errno = errno;
        this.errnoMessage = errnoMessage;
    }

    @Override
    public String getMessage() {
        return String.format(
                "code: %s, strError: %s, strErrorBuffer: %s, errno: %s, errnoMessage: %s",
                code, strError, strErrorBuffer, errno, errnoMessage
        );
    }

    @Override
    public String toString() {
        return String.format("<CURLException %s>", getMessage());
    }
}
