package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts;


import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;

@SeamlessTalkRabbitContract(messageTtl = 600000)
public interface RepositoryContract {

    String myDearRabbit(String data);

    void testSomeData(Integer data);
}
