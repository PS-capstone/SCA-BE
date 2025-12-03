package com.example.sca_be.domain.file.service;

import com.example.sca_be.domain.file.dto.DocumentUploadResponse;
import com.example.sca_be.global.exception.CustomException;
import com.example.sca_be.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 허용된 문서 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf");

    // 허용된 MIME 타입
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf"
    );

    // 최대 파일 크기 (50MB)
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    // PDF 파일 시그니처 (PDF 파일의 시작 바이트)
    private static final byte[] PDF_SIGNATURE = {0x25, 0x50, 0x44, 0x46}; // "%PDF"

    public DocumentUploadResponse uploadDocument(MultipartFile file) throws IOException {
        // 1. 파일 비어있는지 확인
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_DOCUMENT_FILE, "파일이 비어있습니다.");
        }

        // 2. 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.DOCUMENT_TOO_LARGE);
        }

        // 3. 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_DOCUMENT_FILE, "파일명이 없습니다.");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_DOCUMENT_TYPE);
        }

        // 4. MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_DOCUMENT_TYPE);
        }

        // 5. 실제 PDF 파일인지 검증 (PDF 시그니처 확인)
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[4];
            int bytesRead = inputStream.read(header);
            
            if (bytesRead < 4 || !isPdfSignature(header)) {
                throw new CustomException(ErrorCode.INVALID_DOCUMENT_FILE, "유효하지 않은 PDF 파일입니다.");
            }
        } catch (IOException e) {
            log.error("PDF 파일 읽기 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_DOCUMENT_FILE, "PDF 파일을 읽을 수 없습니다.");
        }

        // 6. 파일명 생성 (UUID + 확장자)
        String filename = UUID.randomUUID().toString() + "." + extension;
        String s3Key = "documents/" + filename;

        // 7. S3에 파일 업로드
        try (InputStream uploadStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(uploadStream, file.getSize()));
        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR, "PDF 업로드 중 오류가 발생했습니다.");
        }

        // 8. S3 URL 생성
        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                s3Key);

        log.info("PDF 업로드 성공: {} -> {} (크기: {} bytes)", originalFilename, fileUrl, file.getSize());

        return DocumentUploadResponse.builder()
                .filename(filename)
                .originalFilename(originalFilename)
                .url(fileUrl)
                .size(file.getSize())
                .contentType(contentType)
                .build();
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    private boolean isPdfSignature(byte[] header) {
        if (header.length < 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (header[i] != PDF_SIGNATURE[i]) {
                return false;
            }
        }
        return true;
    }
}
