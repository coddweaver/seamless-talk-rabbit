package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.repositorymanager.contracts;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.repositorymanager.dtos.FileDto;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.repositorymanager.dtos.RepositoryRequestDto;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.repositorymanager.dtos.RepositoryResponseDto;

import java.io.IOException;

@SeamlessTalkRabbitContract
public interface LibraryIOContract {

    FileDto get(String path);

    boolean save(FileDto request);
}
