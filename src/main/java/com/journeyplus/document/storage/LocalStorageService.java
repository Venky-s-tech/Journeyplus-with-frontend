package com.journeyplus.document.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalStorageService implements StorageService {

    private final Path root = Paths.get("data/documents");

    public LocalStorageService() throws IOException {
        File dir = root.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        Path dest = root.resolve(filename);
        Files.copy(file.getInputStream(), dest);
        return dest.toString();
    }

    @Override
    public byte[] load(String path) throws IOException {
        Path p = Paths.get(path);
        return Files.readAllBytes(p);
    }
}
