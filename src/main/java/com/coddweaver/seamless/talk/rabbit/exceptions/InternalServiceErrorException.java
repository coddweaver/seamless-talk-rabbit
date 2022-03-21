package com.coddweaver.seamless.talk.rabbit.exceptions;

/**
 * {@code InternalServiceErrorException} is a base class for all exceptions caused during message processing using SeamlessTalk. Represents
 * any exception. Below is specified exception classes for some frequent cases.
 *
 * <p>You can also create your own exceptions extending this class, but you need to be sure that this classes will be available for all
 * other services. Otherwise it can cause deserialization errors.</p>
 *
 * @author Andrey Buturlakin
 * @see BadRequestException
 * @see ForbiddenException
 * @see NotFoundException
 * @see NotImplementedException
 * @see MethodNotAllowedException
 */
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
