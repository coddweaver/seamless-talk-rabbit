package com.coddweaver.seamless.talk.rabbit.exceptions;

/**
 * Allows you to designate caused exception during message processing as BadRequest.
 *
 * @author Andrey Buturlakin
 * @see InternalServiceErrorException
 */
public class BadRequestException extends InternalServiceErrorException {

    public BadRequestException() {
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    public BadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
