package com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.contracts;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.dtos.RepositoryRequestDto;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.dtos.RepositoryResponseDto;

@SeamlessTalkRabbitContract
public interface LibrarySearchContract {

    RepositoryResponseDto handle(RepositoryRequestDto request);
}
