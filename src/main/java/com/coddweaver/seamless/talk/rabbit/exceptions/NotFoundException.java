package com.coddweaver.seamless.talk.rabbit.exceptions;

/**
 * Allows you to designate caused exception during message processing as NotFound.
 *
 * @author Andrey Buturlakin
 * @see InternalServiceErrorException
 */
public class NotFoundException extends InternalServiceErrorException {

    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    public NotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
