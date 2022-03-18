package com.coddweaver.services.weaver.rabbit.configs.rabbit;

import java.util.Collection;

public interface ExchangeRegisterer {

    Collection<ExchangeDefinition> collectExchanges();

}
