package io.walti.api.exceptions;

public class WaltiApiException extends Exception {

    public WaltiApiException() {}

    public WaltiApiException(String message) { super(message); }

    public WaltiApiException(String message, Throwable cause) { super(message, cause); }

    public WaltiApiException(Throwable cause) {
        super(cause);
    }
}
