package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts;


import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.generation.BaseSeamlessTalkRabbitContract;

@SeamlessTalkRabbitContract(exchangeDefs = {"fanoutTest"}, messageTTL = 600000)
public interface RecognitionContract extends BaseSeamlessTalkRabbitContract {

    String myDearRabbit(String data);

    void testSomeData(Integer data);
}
