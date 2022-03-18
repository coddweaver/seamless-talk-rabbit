package com.coddweaver.services.weaver.rabbit.configs.rabbit;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitListenerFactoryConfig {

    //region Fields
    private final AmqpTemplate amqpTemplate;
//endregion Fields

    //region Constructors
    public RabbitListenerFactoryConfig(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }
//endregion Constructors

    //region Public Methods
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setErrorHandler(new PrettyRabbitErrorHandler(amqpTemplate));
        return factory;
    }
//endregion Public Methods
}
