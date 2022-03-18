package com.coddweaver.services.weaver.rabbit.examples.libraryapi.controllers;

import com.coddweaver.services.weaver.rabbit.examples.libraryapi.services.interfaces.StorageService;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.CustomContract;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.LibraryContract;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.RecognitionContract;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.dtos.FooBarDto;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("library")
public class LibraryController {

    //region Fields
    private final StorageService service;
    private final RecognitionContract recognitionContract;
    private final CustomContract customContract;
    private final LibraryContract libraryContract;
//endregion Fields

    //region Package Private Methods
    @RequestMapping(
            path = "/save",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String saveFile(@RequestParam("file") MultipartFile file, @RequestParam(value = "path", defaultValue = "") String path) {
        service.saveFile(file, path);
        return "Success";
    }

    @GetMapping("/get")
    ResponseEntity<Resource> saveFile(@RequestParam("filepath") String filepath) {
        InputStreamResource resource = new InputStreamResource(service.getFile(filepath));
        return ResponseEntity.ok()
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + Paths.get(filepath)
                                                                                                       .getFileName() + "\"")
                             .contentType(MediaType.APPLICATION_OCTET_STREAM)
                             .body(resource);
    }

    @GetMapping("/processMessage")
    ResponseEntity processMessage(@RequestParam("message") String message) {
        final String answer = libraryContract.processMessage(message);
        return ResponseEntity.ok()
                             .body("Answer is: " + answer);
    }

    @GetMapping("/myDearRabbit")
    ResponseEntity myDearRabbit(@RequestParam("message") String message) {
        final String answer = recognitionContract.myDearRabbit(message);
        return ResponseEntity.ok()
                             .body("Answer is: " + answer);
    }

    @GetMapping("/testRabbit")
    ResponseEntity testRabbit(@RequestParam("message") Integer message) {
        customContract.testRabbit(message);
        return ResponseEntity.ok()
                             .body("Message sended successfully. Look at logs there");
    }

    @GetMapping("/testRabbitRpc")
    ResponseEntity testRabbitRpc(@RequestParam("message") String message) {
        final String answer = customContract.testRabbitRpc(message);
        return ResponseEntity.ok()
                             .body("Got an answer back: \n" + answer);
    }

    @PostMapping("/testRabbitMessageConversion")
    ResponseEntity testRabbitMessageConversion(@RequestBody FooBarDto message) {
        final FooBarDto answer = customContract.testRabbitMessageConversion(message);
        return ResponseEntity.ok()
                             .body("Got an answer back: \n" + answer);
    }

    @GetMapping("/listAllFiles")
    List<String> listAllFiles() {
        return service.listAllFiles();
    }

    @GetMapping("/listAllFolders")
    List<String> listAllFolders() {
        return service.listAllFolders();
    }

    @GetMapping("/listAllFiles/{path}")
    List<String> listAllFilesInPath(@PathVariable("path") String path) {
        return service.listAllFilesInPath(path);
    }

    @GetMapping("/listAllFolders/{path}")
    List<String> listAllFoldersInPath(@PathVariable("path") String path) {
        return service.listAllFoldersInPath(path);
    }

    @GetMapping("/search/{name}")
    List<String> searchFiles(@PathVariable("name") String name) {
        return service.searchFiles(name);
    }
//endregion Package Private Methods

}
