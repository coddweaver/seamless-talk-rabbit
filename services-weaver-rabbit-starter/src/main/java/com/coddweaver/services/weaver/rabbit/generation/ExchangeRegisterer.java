package com.coddweaver.services.weaver.rabbit.generation;

import java.util.Collection;

public interface ExchangeRegisterer {

    Collection<ExchangeDefinition> collectExchanges();

}
