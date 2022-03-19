package com.coddweaver.seamless.talk.rabbit.configs;

import com.coddweaver.seamless.talk.rabbit.generation.QueueGenerator;
import com.coddweaver.seamless.talk.rabbit.helpers.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component("logToDlqRabbitErrorHandler")
public class LogToDlqRabbitErrorHandler implements RabbitListenerErrorHandler {

    private final AmqpTemplate amqpTemplate;


    public LogToDlqRabbitErrorHandler(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendError(Message failedMessage, Throwable throwable) {
        final MessageProperties failedMessageProperties = failedMessage.getMessageProperties();
        final Map<String, Object> messageHeaders = failedMessageProperties.getHeaders();

        messageHeaders.put("x-death-queue", failedMessageProperties.getConsumerQueue());
        messageHeaders.put("x-death-exchange", failedMessageProperties.getReceivedExchange());
        messageHeaders.put("x-death-routing-key", failedMessageProperties.getReceivedRoutingKey());
        messageHeaders.put("x-death-time", System.currentTimeMillis());
        messageHeaders.put("x-death-reason", "rejected");
        messageHeaders.put("x-exception", ExceptionUtils.getRootCause(throwable));
        messageHeaders.put("x-stacktrace", ExceptionUtils.getStackTrace(throwable));

        amqpTemplate.convertAndSend(QueueGenerator.DEFAULT_DLX_NAME, failedMessageProperties.getConsumerQueue(), failedMessage);
        log.error("Failed message log was sent to " + QueueGenerator.DEFAULT_DLX_NAME + " with " + failedMessageProperties.getConsumerQueue() + " routing key" , throwable);
    }

    @Override
    public Object handleError(Message amqpMessage, org.springframework.messaging.Message<?> message,
            ListenerExecutionFailedException exception) {

        final Throwable cause = exception.getCause();
        sendError(exception.getFailedMessage(), cause);
        return cause;
    }
}