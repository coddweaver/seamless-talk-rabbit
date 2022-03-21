package com.coddweaver.seamless.talk.rabbit.examples.libraryapi.controllers;

import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.contracts.LibraryIOContract;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.contracts.LibrarySearchContract;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.dtos.FileDto;
import com.coddweaver.seamless.talk.rabbit.examples.sharedlib.api.repositorymanager.dtos.RepositoryRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryIOContract libraryIOContract;
    private final LibrarySearchContract librarySearchContract;

    @RequestMapping(
            path = "/save",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String saveFile(@RequestParam("file") MultipartFile file, @RequestParam(value = "path", defaultValue = "") String path)
            throws IOException {
        libraryIOContract.save(FileDto.builder()
                                      .data(file.getBytes())
                                      .path(path)
                                      .build());
        return "Success";
    }

    @GetMapping("/get")
    ResponseEntity<Resource> getFile(@RequestParam("filepath") String filepath) {
        final FileDto fileDto = libraryIOContract.get(filepath);
        final Path path = Paths.get(fileDto.getPath());
        return ResponseEntity.ok()
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName()
                                                                                                      .toString() + "\"")
                             .contentType(MediaType.APPLICATION_OCTET_STREAM)
                             .body(new InputStreamResource(new ByteArrayInputStream(fileDto.getData())));
    }

    @GetMapping("/read")
    ResponseEntity<Resource> readFile(@RequestParam("filepath") String filepath) {
        final FileDto fileDto = libraryIOContract.get(filepath);
        final Path path = Paths.get(fileDto.getPath());
        final FileNameMap fileNameMap = URLConnection.getFileNameMap();
        final String contentType = fileNameMap.getContentTypeFor(path.getFileName()
                                                                           .toString());
        return ResponseEntity.ok()
                             .contentType(MediaType.asMediaType(MimeType.valueOf(contentType)))
                             .body(new InputStreamResource(new ByteArrayInputStream(fileDto.getData())));
    }

    @GetMapping("/listAllFiles")
    List<String> listAllFiles() {
        return librarySearchContract.handle(RepositoryRequestDto.builder()
                                                                .operation(RepositoryRequestDto.Operation.LIST_ALL_FILES)
                                                                .build())
                                    .getBody();
    }

    @GetMapping("/listAllFolders")
    List<String> listAllFolders() {
        return librarySearchContract.handle(RepositoryRequestDto.builder()
                                                                .operation(RepositoryRequestDto.Operation.LIST_ALL_FOLDERS)
                                                                .build())
                                    .getBody();
    }

    @GetMapping("/listAllFiles/{path}")
    List<String> listAllFilesInPath(@PathVariable("path") String path) {
        return librarySearchContract.handle(RepositoryRequestDto.builder()
                                                                .body(path)
                                                                .operation(RepositoryRequestDto.Operation.LIST_ALL_FILES_IN_PATH)
                                                                .build())
                                    .getBody();
    }

    @GetMapping("/listAllFolders/{path}")
    List<String> listAllFoldersInPath(@PathVariable("path") String path) {
        return librarySearchContract.handle(RepositoryRequestDto.builder()
                                                                .body(path)
                                                                .operation(RepositoryRequestDto.Operation.LIST_ALL_FOLDERS_IN_PATH)
                                                                .build())
                                    .getBody();
    }

    @GetMapping("/search/{name}")
    List<String> searchFiles(@PathVariable("name") String name) {
        return librarySearchContract.handle(RepositoryRequestDto.builder()
                                                                .body(name)
                                                                .operation(RepositoryRequestDto.Operation.SEARCH_FILES)
                                                                .build())
                                    .getBody();
    }
}
