package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts;


import com.coddweaver.seamless.talk.rabbit.annotations.AutoGenRabbitQueue;
import com.coddweaver.seamless.talk.rabbit.generation.RabbitApi;

@AutoGenRabbitQueue
public interface LibraryContract extends RabbitApi {

    String processMessage(String data);
}
