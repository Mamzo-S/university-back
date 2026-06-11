package com.universite.controller;

import com.universite.file.FileStorageService;
import com.universite.file.FileUploadResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public FileUploadResponse uploadFile(

            @RequestParam("file")
            MultipartFile file
    ) {

        String fileName =
                fileStorageService.storeFile(file);

        String fileDownloadUri =
                "http://localhost:8080/uploads/"
                        + fileName;

        return FileUploadResponse.builder()
                .fileName(fileName)
                .fileDownloadUri(fileDownloadUri)
                .fileType(file.getContentType())
                .size(file.getSize())
                .build();
    }
}