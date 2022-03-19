package com.coddweaver.seamless.talk.rabbit.configs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.coddweaver.seamless.talk.rabbit")
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
public class RabbitTemplateConfig {

    private final RabbitProperties properties;


    public RabbitTemplateConfig(RabbitProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RabbitTemplate jsonRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplateConfigurer configurer = new RabbitTemplateConfigurer(properties);
        RabbitTemplate template = new RabbitTemplate();
        configurer.configure(template, connectionFactory);

        template.setMessageConverter(jsonMessageConverter(generateObjectMapper()));

        return template;
    }

    @Bean
    public ObjectMapper generateObjectMapper() {
        return new ObjectMapper()
                .addHandler(new JacksonExceptionHandler())
                .configure(DeserializationFeature.
                                   FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new ExceptionAwareJsonMessageConverter(objectMapper);
    }
}
