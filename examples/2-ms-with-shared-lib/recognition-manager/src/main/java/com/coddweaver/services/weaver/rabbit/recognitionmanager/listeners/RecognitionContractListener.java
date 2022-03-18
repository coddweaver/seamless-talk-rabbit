package com.coddweaver.services.weaver.rabbit.recognitionmanager.listeners;

import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitListener;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.RecognitionContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

@Slf4j
@AutoGenRabbitListener
public class RecognitionContractListener implements RecognitionContract {

    //region Overriden methods
    @Override
    @RabbitHandler
    public String myDearRabbit(String data) {
        return "Got a message: " + data;
    }
//endregion Overriden Methods

}
