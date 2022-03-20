package com.coddweaver.seamless.talk.rabbit.examples.libraryapi.controllers;

import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.CustomContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.LibraryContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.api.contracts.RepositoryContract;
import com.coddweaver.seamless.talk.rabbit.examples.msisharedlib.dtos.FooBarDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("library")
public class LibraryController {

    private final RepositoryContract repositoryContract;
    private final CustomContract customContract;
    private final LibraryContract libraryContract;


    @GetMapping("/processMessage")
    ResponseEntity processMessage(@RequestParam("message") String message) {
        libraryContract.fanoutMessage(message);
        return ResponseEntity.ok()
                             .build();
    }

    @GetMapping("/myDearRabbit")
    ResponseEntity myDearRabbit(@RequestParam("message") String message) {
        final String answer = repositoryContract.myDearRabbit(message);
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

//    @RequestMapping(
//            path = "/save",
//            method = RequestMethod.POST,
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    String saveFile(@RequestParam("file") MultipartFile file, @RequestParam(value = "path", defaultValue = "") String path) {
//        service.saveFile(file, path);
//        return "Success";
//    }
//
//    @GetMapping("/get")
//    ResponseEntity<Resource> saveFile(@RequestParam("filepath") String filepath) {
//        InputStreamResource resource = new InputStreamResource(service.getFile(filepath));
//        return ResponseEntity.ok()
//                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + Paths.get(filepath)
//                                                                                                       .getFileName() + "\"")
//                             .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                             .body(resource);
//    }
//
//    @GetMapping("/listAllFiles")
//    List<String> listAllFiles() {
//        return service.listAllFiles();
//    }
//
//    @GetMapping("/listAllFolders")
//    List<String> listAllFolders() {
//        return service.listAllFolders();
//    }
//
//    @GetMapping("/listAllFiles/{path}")
//    List<String> listAllFilesInPath(@PathVariable("path") String path) {
//        return service.listAllFilesInPath(path);
//    }
//
//    @GetMapping("/listAllFolders/{path}")
//    List<String> listAllFoldersInPath(@PathVariable("path") String path) {
//        return service.listAllFoldersInPath(path);
//    }
//
//    @GetMapping("/search/{name}")
//    List<String> searchFiles(@PathVariable("name") String name) {
//        return service.searchFiles(name);
//    }

}
