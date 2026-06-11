package com.universite.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(
            MultipartFile file
    ) {

        String fileName =
                StringUtils.cleanPath(
                        file.getOriginalFilename()
                );

        try {

            Path uploadPath =
                    Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {

                Files.createDirectories(uploadPath);
            }

            Path targetLocation =
                    uploadPath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return fileName;

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Impossible de stocker le fichier"
            );
        }
    }
}