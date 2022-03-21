package com.coddweaver.seamless.talk.rabbit.exceptions;

/**
 * Allows you to designate caused exception during message processing as Forbidden.
 *
 * @author Andrey Buturlakin
 * @see InternalServiceErrorException
 */
public class ForbiddenException extends InternalServiceErrorException {

    public ForbiddenException() {
    }

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenException(Throwable cause) {
        super(cause);
    }

    public ForbiddenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
