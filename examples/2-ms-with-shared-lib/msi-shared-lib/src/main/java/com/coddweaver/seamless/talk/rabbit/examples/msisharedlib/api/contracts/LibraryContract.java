package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts;


import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeType;

@SeamlessTalkRabbitContract(exchangeType = ExchangeType.FANOUT)
public interface LibraryContract {

    void fanoutMessage(String data);
}
