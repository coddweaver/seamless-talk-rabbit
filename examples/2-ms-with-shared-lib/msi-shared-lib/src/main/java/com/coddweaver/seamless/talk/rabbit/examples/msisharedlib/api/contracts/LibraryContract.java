package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts;


import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.generation.BaseSeamlessTalkRabbitContract;

@SeamlessTalkRabbitContract
public interface LibraryContract extends BaseSeamlessTalkRabbitContract {

    String processMessage(String data);
}
