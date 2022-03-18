package com.coddweaver.services.weaver.rabbit.configs.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    //region Public Methods
    @Bean
    @Primary
    public ObjectMapper generateObjectMapper() {
        return new ObjectMapper()
                .addHandler(new JacksonExceptionHandler())
                .configure(DeserializationFeature.
                                   FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean("jsonMessageConverter")
    public MessageConverter jsonMessageConverter(@Autowired ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
//endregion Public Methods
}