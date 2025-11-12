package com.example.sca_be.domain.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CreateClassResponse {

    private Integer classId;
    private String className;
    private String grade;
    private String subject;
    private String inviteCode;
    private Integer teacherId;
    private String teacherName;
    private Integer studentCount;
    private LocalDateTime createdAt;
}
