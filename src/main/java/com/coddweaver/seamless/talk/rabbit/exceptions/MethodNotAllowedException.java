package com.coddweaver.seamless.talk.rabbit.exceptions;

/**
 * Allows you to designate caused exception during message processing as MethodNotAllowed.
 *
 * @author Andrey Buturlakin
 * @see InternalServiceErrorException
 */
public class MethodNotAllowedException extends InternalServiceErrorException {

    public MethodNotAllowedException() {
    }

    public MethodNotAllowedException(String message) {
        super(message);
    }

    public MethodNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodNotAllowedException(Throwable cause) {
        super(cause);
    }

    public MethodNotAllowedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
