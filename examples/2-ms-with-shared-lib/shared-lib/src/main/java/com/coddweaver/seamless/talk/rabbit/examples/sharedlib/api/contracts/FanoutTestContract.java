package com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.contracts;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.generation.ExchangeType;

@SeamlessTalkRabbitContract(exchangeType = ExchangeType.FANOUT, dlqEnabled = false)
public interface FanoutTestContract {

    void sendMessage(String data);
}
