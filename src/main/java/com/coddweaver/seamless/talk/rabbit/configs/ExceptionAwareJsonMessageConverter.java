package com.coddweaver.seamless.talk.rabbit.configs;

import com.coddweaver.seamless.talk.rabbit.exceptions.InternalServiceErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractJackson2MessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.MimeTypeUtils;

import java.lang.reflect.Type;

public class ExceptionAwareJsonMessageConverter extends AbstractJackson2MessageConverter {


    public ExceptionAwareJsonMessageConverter(ObjectMapper jsonObjectMapper) {
        this(jsonObjectMapper, "*");
    }

    public ExceptionAwareJsonMessageConverter(ObjectMapper jsonObjectMapper, String... trustedPackages) {
        super(jsonObjectMapper, MimeTypeUtils.parseMimeType(MessageProperties.CONTENT_TYPE_JSON), trustedPackages);
    }

    @Override
    public Object fromMessage(Message message, Object conversionHint) throws MessageConversionException {
        final Object o = super.fromMessage(message, conversionHint);
        if (o instanceof RuntimeException) {
            throw (RuntimeException) o;
        }
        return o;
    }

    @Override
    protected Message createMessage(Object objectToConvert, MessageProperties messageProperties, Type genericType)
            throws MessageConversionException {
        if (objectToConvert instanceof RemoteInvocationResult) {
            RemoteInvocationResult rir = (RemoteInvocationResult) objectToConvert;

            final Throwable exception = rir.getException();
            if (exception != null) {
                objectToConvert = new InternalServiceErrorException(exception);
            } else if (rir.getValue() != null) {
                objectToConvert = rir.getValue();
            }
        }

        if (objectToConvert instanceof Throwable) {
            objectToConvert = new InternalServiceErrorException((Throwable) objectToConvert);
        }

        genericType = objectToConvert.getClass();
        return super.createMessage(objectToConvert, messageProperties, genericType);
    }
}
