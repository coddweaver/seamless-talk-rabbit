package com.coddweaver.services.weaver.rabbit.recognitionmanager.listeners;

import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitListener;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.LibraryContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

@AutoGenRabbitListener
@Slf4j
public class LibraryContractListener implements LibraryContract {

//region Overriden methods
    @Override
    @RabbitHandler
    public String processMessage(String data) {
        log.error("Got a message: {}", data);
        return "Answer your message";
    }
//endregion Overriden Methods
}
