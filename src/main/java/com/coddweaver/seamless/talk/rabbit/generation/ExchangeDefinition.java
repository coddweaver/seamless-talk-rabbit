package com.coddweaver.seamless.talk.rabbit.generation;

import lombok.Getter;

@Getter
public class ExchangeDefinition {

    private final Class<?> contract;
    private final ExchangeType type;
    private final boolean durable;

    public ExchangeDefinition(Class<?> contract, ExchangeType type) {
        this.contract = contract;
        this.type = type;
        this.durable = false;
    }

    public ExchangeDefinition(Class<?> contract, ExchangeType type, boolean durable) {
        this.contract = contract;
        this.type = type;
        this.durable = durable;
    }
}
