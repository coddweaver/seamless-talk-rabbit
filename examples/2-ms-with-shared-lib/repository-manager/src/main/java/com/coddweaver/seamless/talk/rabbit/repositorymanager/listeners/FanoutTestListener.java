package com.coddweaver.seamless.talk.rabbit.repositorymanager.listeners;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitListener;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.contracts.FanoutTestContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

@SeamlessTalkRabbitListener
@Slf4j
public class FanoutTestListener implements FanoutTestContract {

    @Override
    @RabbitHandler
    public void sendMessage(String data) {
        log.info("FanoutTestListener: Got a message '" + data + "'");
    }
}
