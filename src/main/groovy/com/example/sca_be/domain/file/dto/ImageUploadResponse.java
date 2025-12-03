package com.example.sca_be.domain.file.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUploadResponse {
    private String filename;
    private String originalFilename;
    private String url;
    private Long size;
    private String contentType;
    private Integer width;
    private Integer height;
}




