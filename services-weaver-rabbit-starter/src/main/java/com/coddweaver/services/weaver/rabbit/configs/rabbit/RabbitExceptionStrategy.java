package com.coddweaver.services.weaver.rabbit.configs.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

@Slf4j
@SuppressWarnings("unused")
public class RabbitExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {

    //region Overriden methods
    @Override
    public boolean isFatal(Throwable t) {
        return super.isFatal(t);
    }
//endregion Overriden Methods
}
