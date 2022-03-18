package com.coddweaver.services.weaver.rabbit.configs.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

@Slf4j
@SuppressWarnings("unused")
public class RabbitExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {

    @Override
    public boolean isFatal(Throwable t) {
        return super.isFatal(t);
    }
}
