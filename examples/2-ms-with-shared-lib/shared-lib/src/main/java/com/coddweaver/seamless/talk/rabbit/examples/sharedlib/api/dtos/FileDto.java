package com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.dtos;

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
