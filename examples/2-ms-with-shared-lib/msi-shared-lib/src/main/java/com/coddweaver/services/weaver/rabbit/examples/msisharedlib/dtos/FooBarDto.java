package com.coddweaver.services.weaver.rabbit.examples.msisharedlib.dtos;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public class FooBarDto {

    //region Fields
    private String foo;
    private List<Integer> bars;
//endregion Fields
}
