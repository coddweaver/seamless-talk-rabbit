package com.coddweaver.seamless.talk.rabbit.repositorymanager.listeners;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitListener;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.RepositoryContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

@Slf4j
@SeamlessTalkRabbitListener
public class RecognitionContractListener implements RepositoryContract {

    @Override
    @RabbitHandler
    public String myDearRabbit(String data) {
        return "Got a message: " + data;
    }

    @Override
    @RabbitHandler
    public void testSomeData(Integer data) {
        log.warn("Got a message by RecognitionContract.testSomeData: " + data);
    }

}
