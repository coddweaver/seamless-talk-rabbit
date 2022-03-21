package com.coddweaver.seamless.talk.rabbit.generation;

import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.AmqpTemplate;

import java.util.Arrays;

/**
 * Base class for generated Rabbit api classes implementing {@link com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract}.
 * Wraps work with {@link AmqpTemplate}.
 *
 * <p>If amqpTemplate returns the result equal to null (frequently happens on reply timeout expiring), throws an {@link
 * AmqpTimeoutException}.</p>
 *
 * @author Andrey Buturlakin
 * @see com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract
 */
public abstract class AbstractRabbitApi {

    private String getApiMethodName() {
        final StackTraceElement[] allStack = Thread.currentThread()
                                                   .getStackTrace();

        final StackTraceElement first = Arrays.stream(allStack, 1, allStack.length)
                                              .sequential()
                                              .filter(x -> !x.getClassName()
                                                             .equals(AbstractRabbitApi.class.getCanonicalName()))
                                              .findFirst()
                                              .orElseThrow();

        return first.getClassName() + "." + first.getMethodName();
    }

    protected <TPayload> void convertAndSend(AmqpTemplate template, String exchangeName, String routingKey, TPayload payload) {
        if (exchangeName == null) {
            template.convertAndSend(routingKey, payload);
        } else {
            template.convertAndSend(exchangeName, routingKey, payload);
        }
    }

    protected <TPayload, TAnswer> TAnswer convertSendAndReceive(AmqpTemplate template, String exchangeName, String routingKey,
            TPayload payload) throws AmqpTimeoutException {
        if (exchangeName == null) {
            return (TAnswer) checkResponse(template.convertSendAndReceive(routingKey, payload));
        } else {
            return (TAnswer) checkResponse(template.convertSendAndReceive(exchangeName, routingKey, payload));
        }
    }

    private <Reply> Reply checkResponse(Reply response) throws AmqpTimeoutException {
        if (response == null) {
            throw new AmqpTimeoutException("Failed to get answer via " + getApiMethodName());
        }

        return response;
    }
}