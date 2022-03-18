package com.coddweaver.services.weaver.rabbit.annotations;

import org.springframework.stereotype.Service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Service
public @interface AutoGenRabbitListener {


    /**
     * The unique identifier of the container managing for this endpoint.
     * <p>If none is specified an auto-generated one is provided.
     *
     * @return the {@code id} for the container managing for this endpoint.
     *
     * @see org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry#getListenerContainer(String)
     */
    String id() default "";

    /**
     * When {@code true}, a single consumer in the container will have exclusive use of the {@link #queues()}, preventing other consumers
     * from receiving messages from the queues. When {@code true}, requires a concurrency of 1. Default {@code false}.
     *
     * @return the {@code exclusive} boolean flag.
     */
    boolean exclusive() default false;

    /**
     * The priority of this endpoint. Requires RabbitMQ 3.2 or higher. Does not change the container priority by default. Larger numbers
     * indicate higher priority, and both positive and negative numbers can be used.
     *
     * @return the priority for the endpoint.
     */
    String priority() default "";

    /**
     * Reference to a {@link org.springframework.amqp.core.AmqpAdmin AmqpAdmin}. Required if the listener is using auto-delete queues and
     * those queues are configured for conditional declaration. This is the admin that will (re)declare those queues when the container is
     * (re)started. See the reference documentation for more information. If a SpEL expression is provided ({@code #{...}}) the expression
     * can evaluate to an {@link org.springframework.amqp.core.AmqpAdmin} instance or bean name.
     *
     * @return the {@link org.springframework.amqp.core.AmqpAdmin} bean name.
     */
    String admin() default "";

    /**
     * If provided, the listener container for this listener will be added to a bean with this value as its name, of type {@code
     * Collection<MessageListenerContainer>}. This allows, for example, iteration over the collection to start/stop a subset of containers.
     *
     * @return the bean name for the group.
     *
     * @since 1.5
     */
    String group() default "";

    /**
     * Set to "true" to cause exceptions thrown by the listener to be sent to the sender using normal {@code replyTo/@SendTo} semantics.
     * When false, the exception is thrown to the listener container and normal retry/DLQ processing is performed.
     *
     * @return true to return exceptions. If the client side uses a {@code RemoteInvocationAwareMessageConverterAdapter} the exception will
     * be re-thrown. Otherwise, the sender will receive a {@code RemoteInvocationResult} wrapping the exception.
     *
     * @since 2.0
     */
    String returnExceptions() default "";

    /**
     * Set an {@link org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler} to invoke if the listener method throws an
     * exception. A simple String representing the bean name. If a Spel expression (#{...}) is provided, the expression must evaluate to a
     * bean name or a {@link org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler} instance.
     *
     * @return the error handler.
     *
     * @since 2.0
     */
    String errorHandler() default "";

    /**
     * Set the concurrency of the listener container for this listener. Overrides the default set by the listener container factory. Maps to
     * the concurrency setting of the container type.
     * <p>For a
     * {@link org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer SimpleMessageListenerContainer} if this value is a
     * simple integer, it sets a fixed number of consumers in the {@code concurrentConsumers} property. If it is a string with the form
     * {@code "m-n"}, the {@code concurrentConsumers} is set to {@code m} and the {@code maxConcurrentConsumers} is set to {@code n}.
     * <p>For a
     * {@link org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer DirectMessageListenerContainer} it sets the {@code
     * consumersPerQueue} property.
     *
     * @return the concurrency.
     *
     * @since 2.0
     */
    String concurrency() default "";

    /**
     * Set to true or false, to override the default setting in the container factory.
     *
     * @return true to auto start, false to not auto start.
     *
     * @since 2.0
     */
    String autoStartup() default "";

    /**
     * Set the task executor bean name to use for this listener's container; overrides any executor set on the container factory. If a SpEL
     * expression is provided ({@code #{...}}), the expression can either evaluate to a executor instance or a bean name.
     *
     * @return the executor bean name.
     *
     * @since 2.2
     */
    String executor() default "";

    /**
     * Override the container factory {@link org.springframework.amqp.core.AcknowledgeMode} property. Must be one of the valid enumerations.
     * If a SpEL expression is provided, it must evaluate to a {@link String} or {@link org.springframework.amqp.core.AcknowledgeMode}.
     *
     * @return the acknowledgement mode.
     *
     * @since 2.2
     */
    String ackMode() default "";

    /**
     * The bean name of a {@link org.springframework.amqp.rabbit.listener.adapter.ReplyPostProcessor} to post process a response before it
     * is sent. If a SpEL expression is provided ({@code #{...}}), the expression can either evaluate to a post processor instance or a bean
     * name.
     *
     * @return the bean name.
     *
     * @see org.springframework.amqp.rabbit.listener.adapter.AbstractAdaptableMessageListener#setReplyPostProcessor(org.springframework.amqp.rabbit.listener.adapter.ReplyPostProcessor)
     * @since 2.2.5
     */
    String replyPostProcessor() default "";

    /**
     * Override the container factory's message converter used for this listener.
     *
     * @return the message converter bean name. If a SpEL expression is provided ({@code #{...}}), the expression can either evaluate to a
     * converter instance or a bean name.
     *
     * @since 2.3
     */
    String messageConverter() default "";

    /**
     * Used to set the content type of a reply message. Useful when used in conjunction with message converters that can handle multiple
     * content types, such as the {@link org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter}. SpEL expressions
     * and property placeholders are supported. Also useful if you wish to control the final content type property when used with certain
     * converters. This does not apply when the return type is {@link org.springframework.amqp.core.Message} or {@link
     * org.springframework.messaging.Message}; set the content type message property or header respectively, in those cases.
     *
     * @return the content type.
     *
     * @see #converterWinsContentType()
     * @since 2.3
     */
    String replyContentType() default "";

    /**
     * Set to 'false' to override any content type headers set by the message converter with the value of the 'replyContentType' property.
     * Some converters, such as the {@link org.springframework.amqp.support.converter.SimpleMessageConverter} use the payload type and set
     * the content type header appropriately. For example, if you set the 'replyContentType' to "application/json" and use the simple
     * message converter when returning a String containing JSON, the converter will overwrite the content type to 'text/plain'. Set this to
     * false, to prevent that action. This does not apply when the return type is {@link org.springframework.amqp.core.Message} because
     * there is no conversion involved. When returning a {@link org.springframework.messaging.Message}, set the content type message header
     * and {@link org.springframework.amqp.support.AmqpHeaders#CONTENT_TYPE_CONVERTER_WINS} to false.
     *
     * @return false to use the replyContentType.
     *
     * @see #replyContentType()
     * @since 2.3
     */
    String converterWinsContentType() default "true";
}
