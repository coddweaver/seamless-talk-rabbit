package com.coddweaver.seamless.talk.rabbit.exceptions;

public class InternalServiceErrorException extends RuntimeException {

    public InternalServiceErrorException() {
    }

    public InternalServiceErrorException(String message) {
        super(message);
    }

    public InternalServiceErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalServiceErrorException(Throwable cause) {
        super(cause);
    }

    public InternalServiceErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
