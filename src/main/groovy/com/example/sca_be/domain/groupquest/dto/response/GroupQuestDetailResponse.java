package com.example.sca_be.domain.groupquest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GroupQuestDetailResponse {
    @JsonProperty("quest_id")
    private Integer questId;

    @JsonProperty("class_id")
    private Integer classId;

    @JsonProperty("class_name")
    private String className;

    private String template;
    private String title;
    private String content;
    private String status;

    @JsonProperty("reward_coral")
    private Integer rewardCoral;

    private String deadline;

    @JsonProperty("completion_status")
    private CompletionStatusInfo completionStatus;

    @JsonProperty("completion_condition")
    private CompletionCondition completionCondition;

    private List<StudentProgressInfo> students;

    @JsonProperty("created_at")
    private String createdAt;

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
    }

    @Getter
    @Builder
    public static class CompletionCondition {
        private String description;
    }

    @Getter
    @Builder
    public static class StudentProgressInfo {
        @JsonProperty("student_id")
        private Integer studentId;

        @JsonProperty("student_name")
        private String studentName;

        @JsonProperty("class_name")
        private String className;

        @JsonProperty("is_completed")
        private Boolean isCompleted;

        @JsonProperty("completed_at")
        private String completedAt;
    }
}
