package com.coddweaver.seamless.talk.rabbit.annotations;

import org.springframework.stereotype.Service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that marks a method to be the target of a Rabbit message listener on the specified contract annotated with {@link
 * SeamlessTalkRabbitContract}. Processing of @SeamlessTalkRabbitListener annotations is performed by {@link
 * SeamlessTalkRabbitListenerBeanPostProcessor}. During processing in Rabbit will be generated all required structure for working with this
 * contract (queue/exchange/binding).
 *
 * @author Andrey Buturlakin
 * @see SeamlessTalkRabbitListenerBeanPostProcessor
 */

@Retention(RUNTIME)
@Target(TYPE)
@Service
public @interface SeamlessTalkRabbitListener {

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
     * When {@code true}, a single consumer in the container will have exclusive use of the queue, preventing other consumers from receiving
     * messages from the queues. When {@code true}, requires a concurrency of 1. Default {@code false}.
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
     * If provided, the listener container for this listener will be added to a bean with this value as its name, of type {@code
     * Collection<MessageListenerContainer>}. This allows, for example, iteration over the collection to start/stop a subset of containers.
     *
     * @return the bean name for the group.
     */
    String group() default "";

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
     */
    String concurrency() default "";


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
}
