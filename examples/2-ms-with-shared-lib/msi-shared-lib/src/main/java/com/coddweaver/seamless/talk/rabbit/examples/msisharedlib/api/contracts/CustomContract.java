package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts;

import com.coddweaver.seamless.talk.rabbit.annotations.AutoGenRabbitQueue;
import com.coddweaver.seamless.talk.rabbit.generation.RabbitApi;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.dtos.FooBarDto;

@AutoGenRabbitQueue()
public interface CustomContract extends RabbitApi {

    void testRabbit(Integer data);

    String testRabbitRpc(String data);

    FooBarDto testRabbitMessageConversion(FooBarDto data);

}
