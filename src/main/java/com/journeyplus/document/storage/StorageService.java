package com.journeyplus.document.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    String store(MultipartFile file, String filename) throws IOException;
    byte[] load(String path) throws IOException;
    boolean delete(String path) throws IOException;
    boolean exists(String path);
}
