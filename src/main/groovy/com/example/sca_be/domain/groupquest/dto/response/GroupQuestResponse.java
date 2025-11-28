package com.example.sca_be.domain.groupquest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupQuestResponse {
    @JsonProperty("group_quest_id")
    private Integer groupQuestId;

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
    private CompletionStatus completionStatus;

    @JsonProperty("created_at")
    private String createdAt;

    @Getter
    @Builder
    public static class CompletionStatus {
        @JsonProperty("completed_count")
        private Integer completedCount;

        @JsonProperty("required_count")
        private Integer requiredCount;

        @JsonProperty("total_count")
        private Integer totalCount;

        @JsonProperty("is_achievable")
        private Boolean isAchievable;
    }
}
