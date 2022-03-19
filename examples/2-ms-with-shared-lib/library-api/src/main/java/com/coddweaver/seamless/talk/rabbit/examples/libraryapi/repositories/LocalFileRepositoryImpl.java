package com.coddweaver.seamless.talk.rabbit.examples.libraryapi.repositories;

import com.coddweaver.seamless.talk.rabbit.examples.libraryapi.repositories.interfaces.FileRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class LocalFileRepositoryImpl implements FileRepository {

    private final Path repoFolder;

    public LocalFileRepositoryImpl(@Value("${repo.folder}") String repoFolder) {
        this.repoFolder = Paths.get(repoFolder);
    }

    @Override
    public void saveFile(byte[] data, String fileName) throws IOException {
        FileUtils.writeByteArrayToFile(Paths.get(repoFolder.toString(), fileName)
                                            .toFile(), data);
    }

    @Override
    public InputStream getFile(String fileName) throws IOException {
        final Path localPath = Paths.get(repoFolder.toString(), fileName);
        return FileUtils.openInputStream(localPath.toFile());
    }

    @Override
    public List<String> listAllFiles() throws IOException {
        return clearFilePaths(Files.walk(repoFolder)
                                   .filter(Files::isRegularFile), repoFolder);
    }

    @Override
    public List<String> listAllFolders() throws IOException {
        return clearFilePaths(Files.walk(repoFolder)
                                   .filter(Files::isDirectory), repoFolder);
    }

    @Override
    public List<String> listAllFilesInPath(String path) throws IOException {
        final Path localPath = Paths.get(repoFolder.toString(), path);
        return clearFilePaths(Files.walk(localPath)
                                   .filter(Files::isRegularFile), localPath);
    }

    @Override
    public List<String> listAllFoldersInPath(String path) throws IOException {
        final Path localPath = Paths.get(repoFolder.toString(), path);
        return clearFilePaths(Files.walk(localPath)
                                   .filter(Files::isDirectory), localPath);
    }

    @Override
    public List<String> searchFiles(String fileName) throws FileNotFoundException {
        File root = repoFolder.toFile();
        final Collection<File> files = FileUtils.listFiles(root, new WildcardFileFilter("*" + fileName + "*"),
                                                           new WildcardFileFilter("*"));
        return clearFilePaths(files.stream()
                                   .map(file -> file.getAbsoluteFile()
                                                    .toPath()), repoFolder);
    }

    private List<String> clearFilePaths(Stream<Path> pathStream, Path root) {
        return pathStream.map(root::relativize)
                         .map(Path::toString)
                         .filter(Strings::isNotBlank)
                         .collect(Collectors.toList());
    }
}
