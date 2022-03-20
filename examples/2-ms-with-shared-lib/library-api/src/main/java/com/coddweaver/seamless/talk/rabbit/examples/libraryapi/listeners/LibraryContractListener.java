package com.coddweaver.seamless.talk.rabbit.examples.libraryapi.listeners;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitListener;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.LibraryContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

@SeamlessTalkRabbitListener
@Slf4j
public class LibraryContractListener implements LibraryContract {

    @Override
    @RabbitHandler
    public void fanoutMessage(String data) {
        log.error("Got a message: {}", data);
    }
}
