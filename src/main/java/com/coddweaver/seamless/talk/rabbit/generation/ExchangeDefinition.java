package com.coddweaver.seamless.talk.rabbit.generation;

import lombok.Getter;
import org.springframework.beans.factory.BeanNameAware;

import java.util.List;

@Getter
public class ExchangeDefinition implements BeanNameAware {

    private String beanName;
    private final ExchangeType type;
    private final boolean durable;

    public ExchangeDefinition(ExchangeType type) {
        this.type = type;
        this.durable = false;
    }

    public ExchangeDefinition(ExchangeType type, boolean durable) {
        this.type = type;
        this.durable = durable;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
