package com.coddweaver.seamless.talk.rabbit.repositorymanager.listeners;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitListener;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.CustomContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.dtos.FooBarDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@SeamlessTalkRabbitListener()
public class CustomContractListener implements CustomContract {

    private final Random rand = ThreadLocalRandom.current();

    @Override
    @RabbitHandler
    public void testRabbit(Integer data) {
        log.warn("Got a message by CustomContract.testRabbit: " + data);
    }

    @Override
    @RabbitHandler
    public String testRabbitRpc(String data) {
        final UUID answer = UUID.randomUUID();
        throw new IllegalArgumentException("Help me i'm sick!");
    }

    @Override
    @RabbitHandler
    public FooBarDto testRabbitMessageConversion(FooBarDto data) {
        final FooBarDto answer = FooBarDto.builder()
                                          .foo(String.valueOf(rand.nextInt(10000)))
                                          .bars(rand.ints(5)
                                                    .boxed()
                                                    .collect(Collectors.toList()))
                                          .build();
        log.warn("Got a message by CustomContract.testRabbitMessageConversion: " + data + " and answering back: " + answer);
        return answer;
    }
}
