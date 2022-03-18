package com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts;


import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitQueue;
import com.coddweaver.services.weaver.rabbit.generation.RabbitApi;

@AutoGenRabbitQueue(messageTTL = 600000)
public interface RecognitionContract extends RabbitApi {

    String myDearRabbit(String data);
}
