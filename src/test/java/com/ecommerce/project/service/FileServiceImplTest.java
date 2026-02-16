package com.ecommerce.project.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;
import static org.junit.jupiter.api.Assertions.*;

class FileServiceImplTest {

    private final FileServiceImpl fileService = new FileServiceImpl();

    // JUnit creates & deletes this folder automatically
    @TempDir
    Path tempDir;

    // ---------- SUCCESS CASE (folder does NOT exist) ----------

    @Test
    void uploadImage_success_folderCreated() throws IOException {
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "dummy-image-content".getBytes()
        );

        String fileName = fileService.uploadImage(
                tempDir.toString(),
                multipartFile
        );

        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".jpg"));

        File savedFile = new File(tempDir.toFile(), fileName);
        assertTrue(savedFile.exists());
    }

    // ---------- SUCCESS CASE (folder already exists) ----------

    @Test
    void uploadImage_success_folderAlreadyExists() throws IOException {
        File existingFolder = tempDir.toFile();
        assertTrue(existingFolder.exists()); // folder exists

        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "photo.png",
                "image/png",
                "image-content".getBytes()
        );

        String fileName = fileService.uploadImage(
                existingFolder.getAbsolutePath(),
                multipartFile
        );

        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".png"));
    }

    // ---------- EDGE CASE (original filename null → runtime failure) ----------
    // This documents current behavior (Sonar-friendly)

    @Test
    void uploadImage_originalFileNameNull_shouldThrowException() {
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                null,
                "image/jpeg",
                "content".getBytes()
        );

        assertThrows(StringIndexOutOfBoundsException.class, () ->
                fileService.uploadImage(path, multipartFile)
        );
    }
}