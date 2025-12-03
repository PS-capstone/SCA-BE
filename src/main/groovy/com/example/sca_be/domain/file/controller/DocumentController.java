package com.example.sca_be.domain.file.controller;

import com.example.sca_be.domain.file.dto.DocumentUploadResponse;
import com.example.sca_be.domain.file.service.DocumentUploadService;
import com.example.sca_be.global.common.ApiVersion;
import com.example.sca_be.global.exception.CustomException;
import com.example.sca_be.global.exception.ErrorCode;
import com.example.sca_be.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 문서(PDF) 업로드 전용 컨트롤러
 * PDF 파일 검증 및 업로드 기능 제공
 */
@RestController
@RequestMapping(ApiVersion.DOCUMENTS)
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentUploadService documentUploadService;

    /**
     * PDF 문서 업로드
     * POST /api/v1/documents/upload
     * 
     * 지원 형식: pdf
     * 최대 크기: 50MB
     * 
     * @param document 업로드할 PDF 파일
     * @return 업로드된 문서 정보 (URL 포함)
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @RequestParam("document") MultipartFile document) {

        log.info("PDF 업로드 요청 수신: 파일명={}, 크기={}, 타입={}",
                document.getOriginalFilename(), document.getSize(), document.getContentType());

        if (document.isEmpty()) {
            log.warn("빈 파일 업로드 시도");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("파일이 비어있습니다."));
        }

        try {
            DocumentUploadResponse response = documentUploadService.uploadDocument(document);
            log.info("PDF 업로드 성공: {} (크기: {} bytes)", response.getUrl(), response.getSize());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "PDF 문서가 업로드되었습니다."));

        } catch (CustomException e) {
            log.error("PDF 업로드 실패 (CustomException): [{}] {}", e.getErrorCode(), e.getMessage());
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
                    
        } catch (IOException e) {
            log.error("PDF 업로드 실패 (IOException): 파일명={}, 크기={}",
                    document.getOriginalFilename(), document.getSize(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.FILE_UPLOAD_ERROR.name(),
                            "PDF 업로드 중 오류가 발생했습니다: " + e.getMessage()));
                    
        } catch (Exception e) {
            log.error("PDF 업로드 실패 (예상치 못한 오류)", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.FILE_UPLOAD_ERROR.name(),
                            "PDF 업로드 중 예상치 못한 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}




