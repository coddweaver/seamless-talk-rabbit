package com.coddweaver.seamless.talk.rabbit.configs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes {@link RabbitTemplateConfigurer} with default MessageConverter.
 * <p><Also creates a beans for {@link MessageConverter} and {@link ObjectMapper}.</p>
 *
 * @author Andrey Buturlakin
 */
@Configuration
@ComponentScan("com.coddweaver.seamless.talk.rabbit")
public class RabbitConfig {

    private final RabbitProperties properties;


    public RabbitConfig(RabbitProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RabbitTemplateConfigurer jsonRabbitTemplate() {
        RabbitTemplateConfigurer configurer = new RabbitTemplateConfigurer(properties);
        configurer.setMessageConverter(jsonMessageConverter(unknownsIgnoringObjectMapper()));
        return configurer;
    }

    @Bean
    public ObjectMapper unknownsIgnoringObjectMapper() {
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
