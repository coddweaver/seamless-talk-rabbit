package com.coddweaver.seamless.talk.rabbit.generation;

import lombok.Getter;

@Getter
public enum ExchangeType {
    DIRECT(org.springframework.amqp.core.ExchangeTypes.DIRECT),
    FANOUT(org.springframework.amqp.core.ExchangeTypes.FANOUT);

    private final String exchangeTypeCode;

    ExchangeType(String exchangeTypeCode) {
        this.exchangeTypeCode = exchangeTypeCode;
    }
}
