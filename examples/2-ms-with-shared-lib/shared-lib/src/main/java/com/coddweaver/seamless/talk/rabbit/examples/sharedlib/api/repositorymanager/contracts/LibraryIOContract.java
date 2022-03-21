package com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.contracts;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.dtos.FileDto;

@SeamlessTalkRabbitContract
public interface LibraryIOContract {

    FileDto get(String path);

    boolean save(FileDto request);
}
