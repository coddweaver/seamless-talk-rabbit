package com.coddweaver.seamless.talk.rabbit.annotations;

import com.coddweaver.seamless.talk.rabbit.generation.ExchangeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a interface to be the contract in SeamlessTalk processes. Processing of @SeamlessTalkRabbitContract annotations is
 * performed by {@link SeamlessTalkRabbitContractProcessor SeamlessTalkRabbitContractProcessor}. During processing will be auto-generated
 * all required api classes to work with this contract easily. It will contains a realization for every method of marked interface. Messages
 * will be sent using bean of {@link org.springframework.amqp.core.AmqpTemplate} class.
 *
 * <p> You also can register your own bean of {@link org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer} for setting up
 * custom amqp settings, but you will need to be sure that this settings will be the same across all your seamlessly talking modules. </p>
 *
 * @author Andrey Buturlakin
 * @see com.coddweaver.seamless.talk.rabbit.configs.RabbitConfig
 * @see SeamlessTalkRabbitContractProcessor
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SeamlessTalkRabbitContract {

    /**
     * The name of the queue to be generated in Rabbit to which listener will be attached.
     * <p>If none is specified an auto-generated one is provided.</p>
     *
     * @return the queue name to be generated in Rabbit and listen.
     *
     * @see com.coddweaver.seamless.talk.rabbit.generation.RoutesGenerator
     */
    String queueName() default "";

    /**
     * Sets the durable argument for the queue. Default: {@code false}
     *
     * @return the {@code durable} boolean flag.
     *
     * @see com.coddweaver.seamless.talk.rabbit.generation.RoutesGenerator
     */
    boolean durable() default false;


    /**
     * When {@code true}, a single consumer in the container will have exclusive use of the
     * {@link #queues()}, preventing other consumers from receiving messages from the
     * queues. When {@code true}, requires a concurrency of 1. .
     * @return the {@code exclusive} boolean flag.
     */


    /**
     * The type of exchange to be generated in Rabbit to which according queue will be bound. Default: {@code ExchangeType.DIRECT}
     *
     * @return the {@link ExchangeType} enum member.
     *
     * @see com.coddweaver.seamless.talk.rabbit.generation.RoutesGenerator
     */
    ExchangeType exchangeType() default ExchangeType.DIRECT;

    /**
     * Sets the lazy argument for the queue. Default: {@code false}
     *
     * @return the {@code lazy} boolean flag.
     *
     * @see com.coddweaver.seamless.talk.rabbit.generation.RoutesGenerator
     */
    boolean lazy() default false;

    /**
     * Sets the message TTL argument for the queue. Default: {@code 0}
     *
     * @return the {@code lazy} boolean flag.
     *
     * @see com.coddweaver.seamless.talk.rabbit.generation.RoutesGenerator
     */
    int messageTtl() default 0;
}
