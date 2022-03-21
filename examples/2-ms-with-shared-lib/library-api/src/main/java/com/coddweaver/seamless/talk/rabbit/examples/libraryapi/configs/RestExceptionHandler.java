package com.coddweaver.seamless.talk.rabbit.examples.libraryapi.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity runtimeException(RuntimeException exception) {
        log.error("Got an error during controller processing", exception);
        return ResponseEntity.internalServerError().body(exception.getMessage());
    }
}
