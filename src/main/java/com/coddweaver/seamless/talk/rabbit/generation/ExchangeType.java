package com.coddweaver.seamless.talk.rabbit.generation;

import lombok.Getter;

@Getter
public enum ExchangeType {
    DIRECT(org.springframework.amqp.core.ExchangeTypes.DIRECT),
    TOPIC(org.springframework.amqp.core.ExchangeTypes.TOPIC),
    FANOUT(org.springframework.amqp.core.ExchangeTypes.FANOUT),
    HEADERS(org.springframework.amqp.core.ExchangeTypes.HEADERS);

    private final String exchangeTypeCode;

    ExchangeType(String exchangeTypeCode) {
        this.exchangeTypeCode = exchangeTypeCode;
    }
}
