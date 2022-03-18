package com.coddweaver.services.weaver.rabbit.configs.rabbit;

import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.AmqpTemplate;

public interface RabbitApi {

//region Getters And Setters
    private String getApiMethodName() {
        final StackTraceElement stackTraceElement = Thread.currentThread()
                                                          .getStackTrace()[3];
        return stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();
    }
//endregion Getters And Setters

    //region Public Methods
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
//endregion Public Methods

//region Private Methods
    private <Reply> Reply checkResponse(Reply response) throws AmqpTimeoutException {
        if (response == null) {
            throw new AmqpTimeoutException("Failed to get answer from " + getApiMethodName());
        }

        return response;
    }
//endregion Private Methods
}