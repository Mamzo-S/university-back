package com.universite.file;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadResponse {

    private String fileName;

    private String fileDownloadUri;

    private String fileType;

    private long size;
}
