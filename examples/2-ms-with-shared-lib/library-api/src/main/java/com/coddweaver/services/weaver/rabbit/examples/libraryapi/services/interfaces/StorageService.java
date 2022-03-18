package com.coddweaver.services.weaver.rabbit.examples.libraryapi.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface StorageService {

    //region Public Methods
    void saveFile(MultipartFile data, String path);

    InputStream getFile(String path);

    List<String> listAllFiles();

    List<String> listAllFolders();

    List<String> listAllFilesInPath(String path);

    List<String> listAllFoldersInPath(String path);

    List<String> searchFiles(String fileName);
//endregion Public Methods
}