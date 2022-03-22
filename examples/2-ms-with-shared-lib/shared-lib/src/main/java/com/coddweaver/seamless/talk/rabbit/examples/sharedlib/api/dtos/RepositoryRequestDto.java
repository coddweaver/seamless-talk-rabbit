package com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class RepositoryRequestDto {
    private Operation operation;
    private String body;

    public enum Operation {
        LIST_ALL_FILES,
        LIST_ALL_FOLDERS,
        LIST_ALL_FILES_IN_PATH,
        LIST_ALL_FOLDERS_IN_PATH,
        SEARCH_FILES
    }
}
