package com.example.sca_be.domain.file.service;

import com.example.sca_be.domain.file.dto.ImageUploadResponse;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 허용된 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    // 허용된 MIME 타입
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public ImageUploadResponse uploadImage(MultipartFile file) throws IOException {
        // 1. 파일 비어있는지 확인
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FILE, "파일이 비어있습니다.");
        }

        // 2. 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.IMAGE_TOO_LARGE);
        }

        // 3. 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FILE, "파일명이 없습니다.");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        // 4. MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        // 5. 실제 이미지 파일인지 검증 (ImageIO로 읽기 시도)
        BufferedImage image;
        try (InputStream inputStream = file.getInputStream()) {
            image = ImageIO.read(inputStream);
            if (image == null) {
                throw new CustomException(ErrorCode.INVALID_IMAGE_FILE, "유효하지 않은 이미지 파일입니다.");
            }
        } catch (IOException e) {
            log.error("이미지 읽기 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_IMAGE_FILE, "이미지를 읽을 수 없습니다.");
        }

        // 6. 이미지 메타데이터 추출
        int width = image.getWidth();
        int height = image.getHeight();

        // 7. 파일명 생성 (UUID + 확장자)
        String filename = UUID.randomUUID().toString() + "." + extension;
        String s3Key = "images/" + filename;

        // 8. S3에 파일 업로드 (InputStream을 다시 읽어야 하므로 파일을 다시 읽음)
        try (InputStream uploadStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(uploadStream, file.getSize()));
        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 업로드 중 오류가 발생했습니다.");
        }

        // 9. S3 URL 생성
        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                s3Key);

        log.info("이미지 업로드 성공: {} -> {} ({}x{})", originalFilename, fileUrl, width, height);

        return ImageUploadResponse.builder()
                .filename(filename)
                .originalFilename(originalFilename)
                .url(fileUrl)
                .size(file.getSize())
                .contentType(contentType)
                .width(width)
                .height(height)
                .build();
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
