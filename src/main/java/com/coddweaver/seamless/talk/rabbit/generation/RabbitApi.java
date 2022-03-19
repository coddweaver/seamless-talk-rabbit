package com.coddweaver.seamless.talk.rabbit.generation;

import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.AmqpTemplate;

import java.util.Arrays;

public interface RabbitApi {

    private String getApiMethodName() {
        final StackTraceElement[] allStack = Thread.currentThread()
                                                            .getStackTrace();

        final StackTraceElement first = Arrays.stream(allStack, 1, allStack.length)
                                              .sequential()
                                              .filter(x -> !x.getClassName()
                                                             .equals(RabbitApi.class.getCanonicalName()))
                                              .findFirst()
                                              .orElseThrow();

        return first.getClassName() + "." + first.getMethodName();
    }

    default <TPayload> void convertAndSend(AmqpTemplate template, String exchangeName, String routingKey, TPayload payload) {
        if (exchangeName == null) {
            template.convertAndSend(routingKey, payload);
        } else {
            template.convertAndSend(exchangeName, routingKey, payload);
        }
    }

    default <TPayload, TAnswer> TAnswer convertSendAndReceive(AmqpTemplate template, String exchangeName, String routingKey,
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