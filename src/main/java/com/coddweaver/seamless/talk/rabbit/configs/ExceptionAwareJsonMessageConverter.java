package com.coddweaver.seamless.talk.rabbit.configs;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.exceptions.InternalServiceErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractJackson2MessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.MimeTypeUtils;

import java.lang.reflect.Type;

/**
 * The extension of {@link AbstractJackson2MessageConverter} that creates seamless exceptions transporting inside RabbitMQ. It requires
 * listeners returnExceptions feature enabled. It is already enabled by default in {@link com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitListenerBeanPostProcessor}
 *
 * <p>Thrown exceptions inside listener will be wrapped in {@link InternalServiceErrorException} if them are not extending it.</p>
 *
 * <p>When converter gets an message with exception extends {@link InternalServiceErrorException} as the  payload it will throw it after
 * deserializing. It allows to create seamless exceptions transporting between services.</p>
 * <p>
 * It will throw an {@link IllegalStateException} if found some mistakes in SeamlessTalk contract defining.
 *
 * <p>Annotated interfaces can use flexible arguments as defined by {@link SeamlessTalkRabbitContract}.</p>
 *
 * @author Andrey Buturlakin
 * @see SeamlessTalkRabbitContract
 * @see RabbitConfig
 */
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
        if (o instanceof InternalServiceErrorException) {
            throw (InternalServiceErrorException) o;
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
