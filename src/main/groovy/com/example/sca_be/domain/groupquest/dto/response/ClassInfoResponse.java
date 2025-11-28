package com.example.sca_be.domain.groupquest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ClassInfoResponse {
    @JsonProperty("class_id")
    private Integer classId;

    @JsonProperty("class_name")
    private String className;

    @JsonProperty("total_students")
    private Integer totalStudents;

    private List<TemplateInfo> templates;

    @Getter
    @Builder
    public static class TemplateInfo {
        private String code;
        private String name;
        private String description;
    }
}
