package com.coddweaver.seamless.talk.rabbit.generation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExchangeDefinition {

    private final String name;
    private final ExchangeType type;
    private final List<Class<? extends RabbitApi>> contracts;
    private final boolean durable;

    public ExchangeDefinition(String name, ExchangeType type, List<Class<? extends RabbitApi>> contracts) {
        this.name = name;
        this.type = type;
        this.contracts = contracts;
        this.durable = false;
    }
}
