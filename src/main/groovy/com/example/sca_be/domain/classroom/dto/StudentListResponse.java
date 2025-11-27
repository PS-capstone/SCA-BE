package com.example.sca_be.domain.classroom.dto;

<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonProperty;
=======
>>>>>>> 31173bfecbab8de3fb1a27ec81d0030d72e3a49c
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StudentListResponse {

<<<<<<< HEAD
    @JsonProperty("class_id")
    private Integer classId;
    @JsonProperty("class_name")
    private String className;
    @JsonProperty("student_count")
=======
    private Integer classId;
    private String className;
>>>>>>> 31173bfecbab8de3fb1a27ec81d0030d72e3a49c
    private Integer studentCount;
    private List<StudentInfo> students;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StudentInfo {
<<<<<<< HEAD
        @JsonProperty("student_id")
        private Integer studentId;
        private String name;
        @JsonProperty("pending_quests")
        private Integer pendingQuests;  // 임시 하드코딩될 필드
        private Integer coral;
        @JsonProperty("research_data")
=======
        private Integer studentId;
        private String name;
        private Integer pendingQuests;  // 임시 하드코딩될 필드
        private Integer coral;
>>>>>>> 31173bfecbab8de3fb1a27ec81d0030d72e3a49c
        private Integer researchData;
    }
}
