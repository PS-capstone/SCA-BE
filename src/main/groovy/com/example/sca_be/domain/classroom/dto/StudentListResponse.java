package com.example.sca_be.domain.classroom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StudentListResponse {

    @JsonProperty("class_id")
    private Integer classId;
    @JsonProperty("class_name")
    private String className;
    @JsonProperty("student_count")
    private Integer studentCount;
    @JsonProperty("students")
    private List<StudentInfo> students;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StudentInfo {
        @JsonProperty("student_id")
        private Integer studentId;
        @JsonProperty("name")
        private String name;
        @JsonProperty("pending_quests")
        private Integer pendingQuests;  
        @JsonProperty("coral")
        private Integer coral;
        @JsonProperty("research_data")
        private Integer researchData;
        @JsonProperty("initialized")
        private Boolean initialized;
        @JsonProperty("grade")
        private Integer grade;
        @JsonProperty("quest_completion_rate")
        private Integer questCompletionRate;
        @JsonProperty("completed_quests_count")
        private Integer completedQuestsCount;
        @JsonProperty("incomplete_quests_count")
        private Integer incompleteQuestsCount;
    }
}
