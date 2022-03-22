package com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.contracts;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitContract;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.dtos.FileDto;

@SeamlessTalkRabbitContract
public interface LibraryIOContract {

    FileDto get(String path);

    boolean save(FileDto request);
}
