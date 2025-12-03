package com.example.sca_be.domain.file.controller;

import com.example.sca_be.domain.file.dto.ImageUploadResponse;
import com.example.sca_be.domain.file.service.ImageUploadService;
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
 * 이미지 업로드 전용 컨트롤러
 * 이미지 파일 검증 및 메타데이터 추출 기능 제공
 */
@RestController
@RequestMapping(ApiVersion.IMAGES)
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageUploadService imageUploadService;

    /**
     * 이미지 업로드
     * POST /api/v1/images/upload
     * 
     * 지원 형식: jpg, jpeg, png, gif, webp
     * 최대 크기: 10MB
     * 
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지 정보 (URL, 메타데이터 포함)
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @RequestParam("image") MultipartFile file) {

        log.info("이미지 업로드 요청 수신: 파일명={}, 크기={}, 타입={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        if (file.isEmpty()) {
            log.warn("빈 파일 업로드 시도");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("파일이 비어있습니다."));
        }

        try {
            ImageUploadResponse response = imageUploadService.uploadImage(file);
            log.info("이미지 업로드 성공: {} ({}x{})", response.getUrl(), response.getWidth(), response.getHeight());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "이미지가 업로드되었습니다."));

        } catch (CustomException e) {
            log.error("이미지 업로드 실패 (CustomException): [{}] {}", e.getErrorCode(), e.getMessage());
            return ResponseEntity.status(e.getHttpStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
                    
        } catch (IOException e) {
            log.error("이미지 업로드 실패 (IOException): 파일명={}, 크기={}",
                    file.getOriginalFilename(), file.getSize(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.IMAGE_PROCESSING_ERROR.name(),
                            "이미지 처리 중 오류가 발생했습니다: " + e.getMessage()));
                    
        } catch (Exception e) {
            log.error("이미지 업로드 실패 (예상치 못한 오류)", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.IMAGE_PROCESSING_ERROR.name(),
                            "이미지 업로드 중 예상치 못한 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}




