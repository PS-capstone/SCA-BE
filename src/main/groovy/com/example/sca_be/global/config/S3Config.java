package com.example.sca_be.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Slf4j
public class S3Config {

    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // 환경변수가 설정되지 않았거나 더미 값인 경우 기본 S3Client 반환 (로컬 개발용)
        if (accessKey == null || accessKey.isEmpty() || accessKey.equals("dummy-access-key")) {
            log.warn("AWS 자격증명이 설정되지 않았습니다. 더미 S3Client를 사용합니다.");
            return S3Client.builder()
                    .region(Region.of(region))
                    .build();
        }
        
        // 실제 AWS 자격증명으로 S3Client 생성
        log.info("AWS S3 클라이언트를 환경변수 자격증명으로 초기화합니다. Region: {}", region);
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}

