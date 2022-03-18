package com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts;

import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitQueue;
import com.coddweaver.services.weaver.rabbit.generation.RabbitApi;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.dtos.FooBarDto;

@AutoGenRabbitQueue()
public interface CustomContract extends RabbitApi {

    void testRabbit(Integer data);

    String testRabbitRpc(String data);

    FooBarDto testRabbitMessageConversion(FooBarDto data);

}
