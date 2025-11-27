package com.example.sca_be.domain.classroom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ClassListResponse {

<<<<<<< HEAD
    @JsonProperty("teacher_name")
    private String teacherName;
    private List<ClassSummary> classes;
    @JsonProperty("total_count")
=======
    private String teacherName;
    private List<ClassSummary> classes;
>>>>>>> 31173bfecbab8de3fb1a27ec81d0030d72e3a49c
    private Integer totalCount;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ClassSummary {
        @JsonProperty("class_id")
        private Integer classId;
        @JsonProperty("class_name")
        private String className;
        @JsonProperty("student_count")
        private Integer studentCount;
        @JsonProperty("waiting_quest_count")
        private Integer waitingQuestCount;  // 대기 중인 퀘스트 수
    }
}
