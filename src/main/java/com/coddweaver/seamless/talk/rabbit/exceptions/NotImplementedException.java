package com.coddweaver.seamless.talk.rabbit.exceptions;

/**
 * Allows you to designate caused exception during message processing as NotImplemented.
 *
 * @author Andrey Buturlakin
 * @see InternalServiceErrorException
 */
public class NotImplementedException extends InternalServiceErrorException {

    public NotImplementedException() {
    }

    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotImplementedException(Throwable cause) {
        super(cause);
    }

    public NotImplementedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
