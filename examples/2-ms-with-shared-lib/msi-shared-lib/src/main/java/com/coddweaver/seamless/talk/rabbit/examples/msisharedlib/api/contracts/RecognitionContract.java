package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts;


import com.coddweaver.seamless.talk.rabbit.annotations.AutoGenRabbitQueue;
import com.coddweaver.seamless.talk.rabbit.generation.RabbitApi;

@AutoGenRabbitQueue(messageTTL = 600000)
public interface RecognitionContract extends RabbitApi {

    String myDearRabbit(String data);
}
