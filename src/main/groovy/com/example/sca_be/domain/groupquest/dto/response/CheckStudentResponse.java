package com.example.sca_be.domain.groupquest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckStudentResponse {
    @JsonProperty("quest_id")
    private Integer questId;

    @JsonProperty("student_id")
    private Integer studentId;

    @JsonProperty("student_name")
    private String studentName;

    @JsonProperty("is_completed")
    private Boolean isCompleted;

    @JsonProperty("checked_at")
    private String checkedAt;

    @JsonProperty("quest_status")
    private QuestStatusInfo questStatus;

    @Getter
    @Builder
    public static class QuestStatusInfo {
        @JsonProperty("current_status")
        private String currentStatus;

        @JsonProperty("completed_count")
        private Integer completedCount;

        @JsonProperty("required_count")
        private Integer requiredCount;

        @JsonProperty("is_achievable")
        private Boolean isAchievable;
    }
}
