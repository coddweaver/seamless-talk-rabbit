package com.coddweaver.services.weaver.rabbit.recognitionmanager.listeners;

import com.coddweaver.services.weaver.rabbit.annotations.AutoGenRabbitListener;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.CustomContract;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.dtos.FooBarDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@AutoGenRabbitListener(returnExceptions = "true")
public class CustomContractListener implements CustomContract {

    //region Fields
    private final Random rand = ThreadLocalRandom.current();
//endregion Fields

    //region Overriden methods
    @Override
    @RabbitHandler
    public void testRabbit(Integer data) {
        log.warn("Got a message by CustomContract.testRabbit: " + data);
    }

    @Override
    @RabbitHandler
    public String testRabbitRpc(String data) {
        try {
            final UUID answer = UUID.randomUUID();
            throw new IllegalArgumentException("JOPA");
        }
        catch (Exception e) {
            return null;
        }
//        log.warn("Got a message by CustomContract.testRabbitRpc: " + data + " and answering back: " + answer);
//        return answer.toString();
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
//endregion Overriden Methods
}