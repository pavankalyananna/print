package com.pdf.printer.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pdf.printer.dto.FileInfo;

@Service
public class FileStorageService {

    // 1. Add the missing field declaration
    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) throws IOException {
        // 2. Initialize the field properly
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        // Create directory if it doesn't exist
        if (!Files.exists(this.fileStorageLocation)) {
            Files.createDirectories(this.fileStorageLocation);
        }
    }

    public FileInfo storeFile(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        int pageCount = 0;

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        
        // 3. Now this will work because fileStorageLocation is properly declared
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // PDF page count logic
        if (extension.equalsIgnoreCase(".pdf")) {
            File pdfFile = targetLocation.toFile();
            try (PDDocument document = PDDocument.load(pdfFile)) {
                pageCount = document.getNumberOfPages();
            }
        }
        else if (extension.equalsIgnoreCase(".jpg") || 
        		extension.equalsIgnoreCase(".jpeg") || 
        		extension.equalsIgnoreCase(".png")) {
            // For single image formats, the page count is always 1
            pageCount = 1;
        }

        return new FileInfo(uniqueFileName, "/uploads/" + uniqueFileName, pageCount);
    }
}