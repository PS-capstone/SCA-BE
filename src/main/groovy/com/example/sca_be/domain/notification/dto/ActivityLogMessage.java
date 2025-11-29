package com.example.sca_be.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * WebSocket message for student activity logs
 */
@Getter
@Builder
@AllArgsConstructor
public class ActivityLogMessage {

    @JsonProperty("log_id")
    private Long logId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("reward_coral")
    private Integer rewardCoral;

    @JsonProperty("reward_research_data")
    private Integer rewardResearchData;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("time_ago")
    private String timeAgo;
}
