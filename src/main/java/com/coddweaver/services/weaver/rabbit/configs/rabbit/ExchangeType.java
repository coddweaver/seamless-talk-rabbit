package com.coddweaver.services.weaver.rabbit.configs.rabbit;

import lombok.Getter;

@Getter
public enum ExchangeType {
    DIRECT(org.springframework.amqp.core.ExchangeTypes.DIRECT),
    TOPIC(org.springframework.amqp.core.ExchangeTypes.TOPIC),
    FANOUT(org.springframework.amqp.core.ExchangeTypes.FANOUT),
    HEADERS(org.springframework.amqp.core.ExchangeTypes.HEADERS);

    //region Fields
    private final String exchangeTypeCode;
//endregion Fields

    //region Constructors
    ExchangeType(String exchangeTypeCode) {
        this.exchangeTypeCode = exchangeTypeCode;
    }
//endregion Constructors
}
