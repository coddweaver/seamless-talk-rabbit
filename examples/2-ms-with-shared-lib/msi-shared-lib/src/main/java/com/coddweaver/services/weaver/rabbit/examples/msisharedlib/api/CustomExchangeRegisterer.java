package com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api;

import com.coddweaver.services.weaver.rabbit.configs.rabbit.ExchangeDefinition;
import com.coddweaver.services.weaver.rabbit.configs.rabbit.ExchangeRegisterer;
import com.coddweaver.services.weaver.rabbit.configs.rabbit.ExchangeType;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.CustomContract;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.RecognitionContract;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

@Configuration
public class CustomExchangeRegisterer implements ExchangeRegisterer {

    //region Overriden methods
    @Override
    public Collection<ExchangeDefinition> collectExchanges() {
        return List.of(
                new ExchangeDefinition("custom_exchange", ExchangeType.TOPIC,
                                       List.of(RecognitionContract.class, CustomContract.class)
                )
        );
    }
//endregion Overriden Methods
}
