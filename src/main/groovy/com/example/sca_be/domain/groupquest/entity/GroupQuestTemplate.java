package com.example.sca_be.domain.groupquest.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupQuestTemplate {
    ATTENDANCE("출석 체크"),
    ASSIGNMENT("과제 제출"),
    PARTICIPATION("수업 참여"),
    EXAM("학교 시험 점수"),
    OTHER("기타");

    private final String description;
}
