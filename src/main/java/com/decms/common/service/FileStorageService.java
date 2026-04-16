package com.decms.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageRoot;

    public FileStorageService(@Value("${app.storage.root:uploads}") String storagePath) {
        this.storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize storage directory", e);
        }
    }

    public String store(MultipartFile file, String caseId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String cleanCaseId = caseId == null ? "uncategorized" : caseId.replaceAll("[^a-zA-Z0-9-_]", "_");
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "evidence.bin" : file.getOriginalFilename());
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }

        String uniqueName = UUID.randomUUID() + extension;
        Path targetDir = this.storageRoot.resolve(cleanCaseId).normalize();
        if (!targetDir.startsWith(this.storageRoot)) {
            throw new IllegalArgumentException("Invalid storage target path");
        }

        try {
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(uniqueName).normalize();
            if (!targetFile.startsWith(this.storageRoot)) {
                throw new IllegalArgumentException("Invalid file target path");
            }
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return targetFile.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to store uploaded file", e);
        }
    }

    public void deleteIfExists(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return;
        }
        try {
            Path toDelete = Paths.get(storedPath).toAbsolutePath().normalize();
            if (!toDelete.startsWith(this.storageRoot)) {
                return;
            }
            Files.deleteIfExists(toDelete);
        } catch (IOException ignored) {
        }
    }
}
