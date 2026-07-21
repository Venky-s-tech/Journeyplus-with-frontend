package com.journeyplus.document.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path root;
    private final long maxFileSize;

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/jpg"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".pdf", ".jpg", ".jpeg", ".png"
    );

    public LocalStorageService(
            @Value("${file.storage.location:./journeyplus-data/uploads}") String storageLocation,
            @Value("${file.storage.max-file-size:10485760}") long maxFileSize) throws IOException {
        this.root = Paths.get(storageLocation).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
        
        File dir = this.root.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of " + (maxFileSize / (1024 * 1024)) + " MB");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String lowerName = originalName.toLowerCase();
        boolean validExt = ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
        if (!validExt) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, JPG, JPEG, and PNG files are allowed.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            boolean validMime = ALLOWED_MIME_TYPES.stream().anyMatch(t -> t.equalsIgnoreCase(contentType));
            if (!validMime) {
                throw new IllegalArgumentException("Invalid file MIME type: " + contentType + ". Only PDF and image files are allowed.");
            }
        }
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        validateFile(file);

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "receipt.pdf";
        String cleanName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueStoredName = UUID.randomUUID().toString() + "_" + cleanName;

        Path dest = root.resolve(uniqueStoredName).normalize();
        
        // Security Check against Path Traversal
        if (!dest.startsWith(root)) {
            throw new SecurityException("Cannot store file outside current storage directory");
        }

        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toString();
    }

    @Override
    public byte[] load(String path) throws IOException {
        Path p = Paths.get(path).normalize();
        if (!Files.exists(p)) {
            throw new IOException("File not found at path: " + path);
        }
        return Files.readAllBytes(p);
    }

    @Override
    public boolean delete(String path) throws IOException {
        if (path == null || path.isBlank()) return false;
        Path p = Paths.get(path).normalize();
        if (Files.exists(p)) {
            return Files.deleteIfExists(p);
        }
        return false;
    }

    @Override
    public boolean exists(String path) {
        if (path == null || path.isBlank()) return false;
        return Files.exists(Paths.get(path).normalize());
    }

    public Path getRootPath() {
        return root;
    }
}
