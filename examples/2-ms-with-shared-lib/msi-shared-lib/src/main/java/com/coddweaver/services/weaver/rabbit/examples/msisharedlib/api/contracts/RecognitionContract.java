package com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts;


import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitQueue;
import com.coddweaver.services.weaver.rabbit.configs.rabbit.RabbitApi;

@AutoGenRabbitQueue(messageTTL = 600000)
public interface RecognitionContract extends RabbitApi {

    //region Public Methods
    String myDearRabbit(String data);
//endregion Public Methods
}
