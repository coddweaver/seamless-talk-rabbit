package com.coddweaver.seamless.talk.rabbit.examples.libraryapi.controllers;

import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.repositorymanager.contracts.FanoutTestContract;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("test")
public class TestController {

    private final FanoutTestContract fanoutTestContract;

    @GetMapping("/test-fanout")
    ResponseEntity testFanout(@RequestParam("message") String message) {
        fanoutTestContract.sendMessage(message);
        return ResponseEntity.ok()
                             .body("Message sent.");
    }
}
