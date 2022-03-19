package com.coddweaver.seamless.talk.rabbit.recognitionmanager.listeners;

import com.coddweaver.seamless.talk.rabbit.annotations.AutoGenRabbitListener;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.LibraryContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

@AutoGenRabbitListener
@Slf4j
public class LibraryContractListener implements LibraryContract {

    @Override
    @RabbitHandler
    public String processMessage(String data) {
        log.error("Got a message: {}", data);
        return "Answer your message";
    }
}
