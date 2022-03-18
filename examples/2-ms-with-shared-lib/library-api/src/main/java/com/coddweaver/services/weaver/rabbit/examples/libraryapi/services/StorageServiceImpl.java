package com.coddweaver.services.weaver.rabbit.examples.libraryapi.services;

import com.coddweaver.services.weaver.rabbit.examples.libraryapi.repositories.interfaces.FileRepository;
import com.coddweaver.services.weaver.rabbit.examples.libraryapi.services.interfaces.StorageService;
import com.coddweaver.services.weaver.rabbit.examples.msisharedlib.api.contracts.RecognitionContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class StorageServiceImpl implements StorageService {

    //region Fields
    private final FileRepository repository;
//endregion Fields

    //region Constructors
    public StorageServiceImpl(@Autowired FileRepository repository) {
        this.repository = repository;
    }
//endregion Constructors

    //region Overriden methods
    @Override
    public void saveFile(MultipartFile data, String fileName) {
        try {
            repository.saveFile(data.getBytes(), fileName);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getFile(String fileName) {
        try {
            return repository.getFile(fileName);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listAllFiles() {
        try {
            return repository.listAllFiles();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listAllFolders() {
        try {
            return repository.listAllFolders();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listAllFilesInPath(String path) {
        try {
            return repository.listAllFilesInPath(path);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listAllFoldersInPath(String path) {
        try {
            return repository.listAllFoldersInPath(path);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> searchFiles(String fileName) {
        try {
            return repository.searchFiles(fileName);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//endregion Overriden Methods
}
