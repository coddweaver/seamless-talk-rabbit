package com.coddweaver.seamless.talk.rabbit.recognitionmanager.listeners;

import com.coddweaver.seamless.talk.rabbit.annotations.AutoGenRabbitListener;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.RecognitionContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

@Slf4j
@AutoGenRabbitListener
public class RecognitionContractListener implements RecognitionContract {

    @Override
    @RabbitHandler
    public String myDearRabbit(String data) {
        return "Got a message: " + data;
    }

}
