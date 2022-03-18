package com.coddweaver.services.weaver.rabbit.configs;

import com.coddweaver.services.weaver.rabbit.generation.QueueGenerator;
import com.coddweaver.services.weaver.rabbit.helpers.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.util.ErrorHandler;

import java.util.Map;

@Slf4j
public class PrettyRabbitErrorHandler implements ErrorHandler {

    private final AmqpTemplate amqpTemplate;


    public PrettyRabbitErrorHandler(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public void handleError(Throwable t) {
        ListenerExecutionFailedException lefe = (ListenerExecutionFailedException) t;

        if (!QueueGenerator.BASIC_DLQ_ENABLED) {
            Message failedMessage = lefe.getFailedMessage();
            final MessageProperties failedMessageProperties = failedMessage.getMessageProperties();
            final Map<String, Object> messageHeaders = failedMessageProperties.getHeaders();

            messageHeaders.put("x-death-queue", failedMessageProperties.getConsumerQueue());
            messageHeaders.put("x-death-exchange", failedMessageProperties.getReceivedExchange());
            messageHeaders.put("x-death-routing-key", failedMessageProperties.getReceivedRoutingKey());
            messageHeaders.put("x-death-time", System.currentTimeMillis());
            messageHeaders.put("x-death-reason", "rejected");
            messageHeaders.put("x-exception", NestedExceptionUtils.getRootCause(lefe));
            messageHeaders.put("x-stacktrace", ExceptionUtils.getStackTrace(lefe));

            amqpTemplate.convertAndSend(QueueGenerator.DEFAULT_DLX_NAME, failedMessage.getMessageProperties()
                                                                                      .getConsumerQueue(), failedMessage);
            //We already processed dead message. Acking it.
            throw new ImmediateAcknowledgeAmqpException(lefe);
        }
    }
}