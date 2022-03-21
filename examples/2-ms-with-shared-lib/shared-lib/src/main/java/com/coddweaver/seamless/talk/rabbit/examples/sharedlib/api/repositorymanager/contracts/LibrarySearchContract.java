package com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.contracts;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.dtos.RepositoryRequestDto;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.dtos.RepositoryResponseDto;

@SeamlessTalkRabbitContract
public interface LibrarySearchContract {

    RepositoryResponseDto handle(RepositoryRequestDto request);
}
