package com.coddweaver.seamless.talk.rabbit.repositorymanager.listeners;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitListener;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.repositorymanager.contracts.LibraryIOContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.repositorymanager.dtos.FileDto;
import com.coddweaver.seamless.talk.rabbit.repositorymanager.services.interfaces.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@SeamlessTalkRabbitListener
@RequiredArgsConstructor
public class LibraryIOListener implements LibraryIOContract {

    private final StorageService storageService;

    @Override
    @RabbitHandler
    public FileDto get(String filePath) {
        try {
            final InputStream file = storageService.getFile(filePath);
            return FileDto.builder()
                          .data(file.readAllBytes())
                          .path(filePath)
                          .build();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @RabbitHandler
    public boolean save(FileDto request) {
        storageService.saveFile(request.getData(), request.getPath());
        return true;
    }
}
