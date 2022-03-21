package com.coddweaver.seamless.talk.rabbit.repositorymanager.listeners;

import com.coddweaver.seamless.talk.rabbit.annotations.SeamlessTalkRabbitListener;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.contracts.LibrarySearchContract;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.dtos.RepositoryRequestDto;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.dtos.RepositoryResponseDto;
import com.coddweaver.seamless.talk.rabbit.exceptions.NotImplementedException;
import com.coddweaver.seamless.talk.rabbit.repositorymanager.services.interfaces.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

import java.util.List;
import java.util.function.Supplier;

@SeamlessTalkRabbitListener
@RequiredArgsConstructor
public class LibrarySearchListener implements LibrarySearchContract {

    private final StorageService storageService;

    @Override
    @RabbitHandler
    public RepositoryResponseDto handle(RepositoryRequestDto request) {
        switch (request.getOperation()) {
            case SEARCH_FILES:
                return prepareResponse(() -> storageService.searchFiles(request.getBody()));
            case LIST_ALL_FILES:
                return prepareResponse(storageService::listAllFiles);
            case LIST_ALL_FILES_IN_PATH:
                return prepareResponse(() -> storageService.listAllFilesInPath(request.getBody()));
            case LIST_ALL_FOLDERS:
                return prepareResponse(storageService::listAllFolders);
            case LIST_ALL_FOLDERS_IN_PATH:
                return prepareResponse(() -> storageService.listAllFoldersInPath(request.getBody()));
            default:
                throw new NotImplementedException(request.getOperation() + " is not supported");
        }
    }

    private RepositoryResponseDto prepareResponse(Supplier<List<String>> resultSupplier) {
        return RepositoryResponseDto.builder()
                                    .body(resultSupplier.get())
                                    .build();
    }
}
