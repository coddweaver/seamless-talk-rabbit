package com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.repositorymanager.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class FileDto {
    private byte[] data;
    private String path;
}
