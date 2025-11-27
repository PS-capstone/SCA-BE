package com.example.sca_be.domain.groupquest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StudentQuestDetailResponse {
    @JsonProperty("quest_id")
    private Integer questId;

    private String template;
    private String title;
    private String content;
    private String status;

    @JsonProperty("reward_coral")
    private Integer rewardCoral;

    @JsonProperty("reward_research_data")
    private Integer rewardResearchData;

    private String deadline;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("completion_status")
    private CompletionStatusInfo completionStatus;

    @JsonProperty("my_status")
    private MyStatusInfo myStatus;

    @JsonProperty("completed_students")
    private List<CompletedStudentInfo> completedStudents;

    @JsonProperty("incomplete_students")
    private List<IncompleteStudentInfo> incompleteStudents;

    @Getter
    @Builder
    public static class CompletionStatusInfo {
        @JsonProperty("completed_count")
        private Integer completedCount;

        @JsonProperty("required_count")
        private Integer requiredCount;

        @JsonProperty("total_count")
        private Integer totalCount;

        @JsonProperty("completion_rate")
        private Integer completionRate;

        @JsonProperty("is_achievable")
        private Boolean isAchievable;

        @JsonProperty("completion_condition_text")
        private String completionConditionText;
    }

    @Getter
    @Builder
    public static class MyStatusInfo {
        @JsonProperty("student_id")
        private Integer studentId;

        @JsonProperty("student_name")
        private String studentName;

        @JsonProperty("is_completed")
        private Boolean isCompleted;

        @JsonProperty("completed_at")
        private String completedAt;

        @JsonProperty("status_text")
        private String statusText;
    }

    @Getter
    @Builder
    public static class CompletedStudentInfo {
        @JsonProperty("student_id")
        private Integer studentId;

        @JsonProperty("student_name")
        private String studentName;

        @JsonProperty("completed_at")
        private String completedAt;
    }

    @Getter
    @Builder
    public static class IncompleteStudentInfo {
        @JsonProperty("student_id")
        private Integer studentId;

        @JsonProperty("student_name")
        private String studentName;

        @JsonProperty("status_text")
        private String statusText;
    }
}
