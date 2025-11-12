package com.example.sca_be.domain.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ClassListResponse {

    private String teacherName;
    private List<ClassSummary> classes;
    private Integer totalCount;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ClassSummary {
        private Integer classId;
        private String className;
        private Integer studentCount;
        private Integer ongoingQuestCount;  // 임시 하드코딩될 필드
        private String createdAt;
    }
}
