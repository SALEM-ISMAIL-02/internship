package com.example.elearning.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * SRP: owns ONLY filesystem persistence of uploaded binaries.
 * Knows nothing about {@code Material}, courses or text extraction.
 */
@Component
public class FileStorageService {

    private final String uploadDir;

    public FileStorageService(@org.springframework.beans.factory.annotation.Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /** Stores the multipart file and returns the absolute target path. */
    public Path store(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path target = Paths.get(uploadDir, fileName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }
}
