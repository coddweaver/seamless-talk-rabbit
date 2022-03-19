package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api;


import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.CustomContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.LibraryContract;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeType;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeDefinition;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeRegisterer;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

@Configuration
public class CustomExchangeRegisterer2 implements ExchangeRegisterer {

    @Override
    public Collection<ExchangeDefinition> collectExchanges() {
        return List.of(
                new ExchangeDefinition("fanout_exchange", ExchangeType.FANOUT,
                                       List.of(LibraryContract.class, CustomContract.class)
                )
        );
    }
}
