package com.example.sca_be.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor
public class TeacherProfileResponse {

    @JsonProperty("teacher_id")
    private Integer teacherId;

    private String username;

    @JsonProperty("real_name")
    private String realName;

    private String nickname;

    private String email;

    private String role;

    @JsonProperty("created_at")
    private String createdAt;
}

