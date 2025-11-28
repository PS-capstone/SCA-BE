package com.example.sca_be.domain.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * AI 관련 설정
 * - Spring AI ChatClient 빈 등록
 * - 비동기 작업 설정 (@Async)
 * - 재시도 설정 (@Retryable)
 */
@Configuration
@EnableAsync
@EnableRetry
public class AiConfig {

    /**
     * ChatClient 빈 생성
     * application.yaml에서 설정된 OpenAI 옵션을 자동으로 사용
     *
     * @param builder ChatClient.Builder (Spring AI Auto-configuration에서 제공)
     * @return ChatClient 인스턴스
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * 비동기 작업용 ThreadPoolTaskExecutor 설정
     * 학습 엔진 비동기 처리에 사용
     *
     * @return Executor
     */
    @Bean(name = "aiLearningExecutor")
    public Executor aiLearningExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AI-Learning-");
        executor.initialize();
        return executor;
    }
}
