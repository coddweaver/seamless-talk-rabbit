package com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts;


import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitQueue;
import com.coddweaver.services.weaver.rabbit.configs.rabbit.RabbitApi;

@AutoGenRabbitQueue
public interface LibraryContract extends RabbitApi {

    String processMessage(String data);
}
