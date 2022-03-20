package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.generation.BaseSeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.dtos.FooBarDto;

@SeamlessTalkRabbitContract(exchangeDefs = {"topicTest", "fanoutTest"})
public interface CustomContract extends BaseSeamlessTalkRabbitContract {

    void testRabbit(Integer data);

    String testRabbitRpc(String data);

    FooBarDto testRabbitMessageConversion(FooBarDto data);

}
