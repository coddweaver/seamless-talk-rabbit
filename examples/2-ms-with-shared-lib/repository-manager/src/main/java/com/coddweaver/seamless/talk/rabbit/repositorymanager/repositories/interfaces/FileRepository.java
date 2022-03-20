package com.coddweaver.seamless.talk.rabbit.repositorymanager.repositories.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileRepository {

    void saveFile(byte[] data, String path) throws IOException;

    InputStream getFile(String path) throws IOException;

    List<String> listAllFiles() throws IOException;

    List<String> listAllFolders() throws IOException;

    List<String> listAllFilesInPath(String path) throws IOException;

    List<String> listAllFoldersInPath(String path) throws IOException;

    List<String> searchFiles(String fileName) throws FileNotFoundException;

}
