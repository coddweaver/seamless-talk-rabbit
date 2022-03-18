package com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts;

import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitQueue;
import com.coddweaver.services.weaver.rabbit.configs.rabbit.RabbitApi;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.dtos.FooBarDto;

@AutoGenRabbitQueue()
public interface CustomContract extends RabbitApi {

    //region Public Methods
    void testRabbit(Integer data);

    String testRabbitRpc(String data);

    FooBarDto testRabbitMessageConversion(FooBarDto data);

//endregion Public Methods
}
