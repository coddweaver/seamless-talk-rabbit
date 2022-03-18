package com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api;


import com.coddweaver.services.weaver.rabbit.generation.ExchangeDefinition;
import com.coddweaver.services.weaver.rabbit.generation.ExchangeRegisterer;
import com.coddweaver.services.weaver.rabbit.generation.ExchangeType;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.CustomContract;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.LibraryContract;
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
