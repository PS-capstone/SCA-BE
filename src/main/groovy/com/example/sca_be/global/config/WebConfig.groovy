package com.example.sca_be.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.List

@Configuration
class WebConfig implements WebMvcConfigurer {
    
    @Override
    void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 기존 컨버터들의 인코딩을 UTF-8로 강제 설정
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter) {
                StringHttpMessageConverter stringConverter = (StringHttpMessageConverter) converter
                stringConverter.setDefaultCharset(StandardCharsets.UTF_8)
                List<MediaType> mediaTypes = new ArrayList<>()
                for (MediaType mt : stringConverter.getSupportedMediaTypes()) {
                    mediaTypes.add(new MediaType(mt.getType(), mt.getSubtype(), StandardCharsets.UTF_8))
                }
                stringConverter.setSupportedMediaTypes(mediaTypes)
            } else if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter
                jsonConverter.setDefaultCharset(StandardCharsets.UTF_8)
                List<MediaType> mediaTypes = new ArrayList<>()
                for (MediaType mt : jsonConverter.getSupportedMediaTypes()) {
                    mediaTypes.add(new MediaType(mt.getType(), mt.getSubtype(), StandardCharsets.UTF_8))
                }
                jsonConverter.setSupportedMediaTypes(mediaTypes)
                // Java 8 시간 타입 지원
                ObjectMapper objectMapper = jsonConverter.getObjectMapper()
                if (objectMapper != null) {
                    objectMapper.registerModule(new JavaTimeModule())
                    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }
            }
        }
    }
    
    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper()
        // Java 8 시간 타입 지원 (LocalDateTime 등)
        mapper.registerModule(new JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        // UTF-8 인코딩 보장
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }
}

