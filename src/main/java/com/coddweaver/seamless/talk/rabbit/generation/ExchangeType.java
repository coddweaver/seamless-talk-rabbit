package com.coddweaver.seamless.talk.rabbit.generation;

import lombok.Getter;

/**
 * Constants for the standard Exchange types. TypeCodes refers to {@link org.springframework.amqp.core.ExchangeTypes}
 *
 * @author Andrey Buturlakin
 * @see com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract
 */
@Getter
public enum ExchangeType {
    DIRECT(org.springframework.amqp.core.ExchangeTypes.DIRECT),
    FANOUT(org.springframework.amqp.core.ExchangeTypes.FANOUT);

    private final String exchangeTypeCode;

    ExchangeType(String exchangeTypeCode) {
        this.exchangeTypeCode = exchangeTypeCode;
    }
}
