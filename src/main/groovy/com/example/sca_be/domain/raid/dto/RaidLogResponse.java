package com.example.sca_be.domain.raid.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RaidLogResponse {

    @JsonProperty("logs")
    private List<RaidLogItem> logs;

    @JsonProperty("total_count")
    private Long totalCount;

    @JsonProperty("page")
    private int page;

    @JsonProperty("size")
    private int size;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RaidLogItem {
        @JsonProperty("log_id")
        private Long logId;
        @JsonProperty("student_name")
        private String studentName;
        @JsonProperty("damage")
        private Integer damage;
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
        @JsonProperty("time_ago")
        private String timeAgo;
    }
}

