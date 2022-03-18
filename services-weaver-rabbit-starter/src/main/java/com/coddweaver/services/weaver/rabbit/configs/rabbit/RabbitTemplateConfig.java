package com.coddweaver.services.weaver.rabbit.configs.rabbit;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.coddweaver.services.weaver.rabbit")
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
public class RabbitTemplateConfig {

    private final MessageConverter converter;
    private final RabbitProperties properties;


    public RabbitTemplateConfig(@Qualifier("jsonMessageConverter") MessageConverter converter,
            RabbitProperties properties) {
        this.converter = converter;
        this.properties = properties;
    }

    @Bean
    public RabbitTemplate jsonRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplateConfigurer configurer = new RabbitTemplateConfigurer(properties);
        RabbitTemplate template = new RabbitTemplate();
        configurer.configure(template, connectionFactory);

        template.setMessageConverter(converter);

        return template;
    }
}
