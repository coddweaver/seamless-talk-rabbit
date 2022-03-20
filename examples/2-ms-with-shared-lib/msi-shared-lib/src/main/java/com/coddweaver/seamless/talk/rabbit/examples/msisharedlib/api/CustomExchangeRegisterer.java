package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api;

import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.CustomContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.LibraryContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.RecognitionContract;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeDefinition;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CustomExchangeRegisterer {

    @Bean
    public ExchangeDefinition topicTest() {
        return new ExchangeDefinition(ExchangeType.TOPIC);
    }

    @Bean
    public ExchangeDefinition fanoutTest() {
        return new ExchangeDefinition(ExchangeType.FANOUT);
    }

}
