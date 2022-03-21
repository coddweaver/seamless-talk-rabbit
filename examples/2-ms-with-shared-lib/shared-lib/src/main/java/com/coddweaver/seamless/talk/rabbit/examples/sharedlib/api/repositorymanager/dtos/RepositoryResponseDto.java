package com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder
public class RepositoryResponseDto {
    private List<String> body;
}
