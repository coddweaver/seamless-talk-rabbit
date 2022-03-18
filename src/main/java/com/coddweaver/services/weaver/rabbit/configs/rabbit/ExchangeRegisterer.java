package com.coddweaver.services.weaver.rabbit.configs.rabbit;

import java.util.Collection;

public interface ExchangeRegisterer {

    //region Public Methods
    Collection<ExchangeDefinition> collectExchanges();
//endregion Public Methods

}
