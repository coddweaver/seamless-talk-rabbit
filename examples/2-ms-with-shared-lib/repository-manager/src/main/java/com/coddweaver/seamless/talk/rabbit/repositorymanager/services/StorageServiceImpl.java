package com.coddweaver.seamless.talk.rabbit.repositorymanager.services;

import com.coddweaver.seamless.talk.rabbit.exceptions.InternalServiceErrorException;
import com.coddweaver.seamless.talk.rabbit.repositorymanager.repositories.interfaces.FileRepository;
import com.coddweaver.seamless.talk.rabbit.repositorymanager.services.interfaces.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class StorageServiceImpl implements StorageService {

    private final FileRepository repository;

    public StorageServiceImpl(@Autowired FileRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveFile(byte[] data, String fileName) {
        try {
            repository.saveFile(data, fileName);
        }
        catch (IOException e) {
            throw new InternalServiceErrorException(e);
        }
    }

    @Override
    public InputStream getFile(String fileName) {
        try {
            return repository.getFile(fileName);
        }
        catch (IOException e) {
            throw new InternalServiceErrorException(e);
        }
    }

    @Override
    public List<String> listAllFiles() {
        try {
            return repository.listAllFiles();
        }
        catch (IOException e) {
            throw new InternalServiceErrorException(e);
        }
    }

    @Override
    public List<String> listAllFolders() {
        try {
            return repository.listAllFolders();
        }
        catch (IOException e) {
            throw new InternalServiceErrorException(e);
        }
    }

    @Override
    public List<String> listAllFilesInPath(String path) {
        try {
            return repository.listAllFilesInPath(path);
        }
        catch (IOException e) {
            throw new InternalServiceErrorException(e);
        }
    }

    @Override
    public List<String> listAllFoldersInPath(String path) {
        try {
            return repository.listAllFoldersInPath(path);
        }
        catch (IOException e) {
            throw new InternalServiceErrorException(e);
        }
    }

    @Override
    public List<String> searchFiles(String fileName) {
        try {
            return repository.searchFiles(fileName);
        }
        catch (IOException e) {
            throw new InternalServiceErrorException(e);
        }
    }
}
