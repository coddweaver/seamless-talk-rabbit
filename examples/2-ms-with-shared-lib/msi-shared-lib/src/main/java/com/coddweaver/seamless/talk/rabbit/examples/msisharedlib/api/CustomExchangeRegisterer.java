package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api;

import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.CustomContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.RecognitionContract;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeType;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeDefinition;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeRegisterer;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

@Configuration
public class CustomExchangeRegisterer implements ExchangeRegisterer {

    @Override
    public Collection<ExchangeDefinition> collectExchanges() {
        return List.of(
                new ExchangeDefinition("custom_exchange", ExchangeType.TOPIC,
                                       List.of(RecognitionContract.class, CustomContract.class)
                )
        );
    }
}
