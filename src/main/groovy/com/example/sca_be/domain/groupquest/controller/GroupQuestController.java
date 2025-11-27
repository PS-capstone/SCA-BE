package com.example.sca_be.domain.groupquest.controller;

import com.example.sca_be.domain.groupquest.dto.request.CheckStudentRequest;
import com.example.sca_be.domain.groupquest.dto.request.CreateGroupQuestRequest;
import com.example.sca_be.domain.groupquest.dto.response.*;
import com.example.sca_be.domain.groupquest.entity.GroupQuestStatus;
import com.example.sca_be.domain.groupquest.service.GroupQuestService;
import com.example.sca_be.global.common.ApiResponse;
import com.example.sca_be.global.security.principal.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quests/group")
@RequiredArgsConstructor
public class GroupQuestController {

    private final GroupQuestService groupQuestService;

    @GetMapping("/class-info")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<ClassInfoResponse>> getClassInfo(
            Authentication authentication,
            @RequestParam("class_id") Integer classId) {

        Integer teacherId = getTeacherId(authentication);

        ClassInfoResponse response = groupQuestService.getClassInfo(classId, teacherId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<GroupQuestResponse>> createGroupQuest(
            Authentication authentication,
            @Valid @RequestBody CreateGroupQuestRequest request) {

        Integer teacherId = getTeacherId(authentication);

        try {
            GroupQuestResponse response = groupQuestService.createGroupQuest(request, teacherId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("단체 퀘스트가 생성되었습니다.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_INPUT"));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<GroupQuestListResponse>> getGroupQuestList(
            Authentication authentication,
            @RequestParam("class_id") Integer classId,
            @RequestParam(value = "status", required = false) String status) {

        GroupQuestStatus questStatus = null;
        if (status != null) {
            try {
                questStatus = GroupQuestStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("INVALID_STATUS", "유효하지 않은 상태값입니다."));
            }
        }

        GroupQuestListResponse response = groupQuestService.getGroupQuestList(classId, questStatus);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{questId}/detail")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<GroupQuestDetailResponse>> getGroupQuestDetail(
            Authentication authentication,
            @PathVariable("questId") Integer questId) {

        try {
            GroupQuestDetailResponse response = groupQuestService.getGroupQuestDetail(questId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("GROUP_QUEST_NOT_FOUND", e.getMessage()));
        }
    }

    @PostMapping("/{questId}/students/{studentId}/check")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<CheckStudentResponse>> checkStudent(
            Authentication authentication,
            @PathVariable("questId") Integer questId,
            @PathVariable("studentId") Integer studentId,
            @Valid @RequestBody CheckStudentRequest request) {

        try {
            CheckStudentResponse response = groupQuestService.checkStudent(
                    questId, studentId, request.getIsCompleted());

            String message = response.getQuestStatus().getIsAchievable() ?
                    "학생 달성이 체크되었습니다. 완료 조건을 달성했습니다!" :
                    "학생 달성이 체크되었습니다.";

            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("STUDENT_NOT_FOUND", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("GROUP_QUEST_ALREADY_COMPLETED", e.getMessage()));
        }
    }

    @DeleteMapping("/{questId}/students/{studentId}/check")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<CheckStudentResponse>> uncheckStudent(
            Authentication authentication,
            @PathVariable("questId") Integer questId,
            @PathVariable("studentId") Integer studentId) {

        try {
            CheckStudentResponse response = groupQuestService.uncheckStudent(questId, studentId);

            String message = response.getQuestStatus().getIsAchievable() ?
                    "학생 달성이 취소되어, 조건 충족이 취소되었습니다." :
                    "학생 달성 체크가 취소되었습니다.";

            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("STUDENT_NOT_FOUND", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("GROUP_QUEST_ALREADY_COMPLETED", e.getMessage()));
        }
    }

    @PostMapping("/{questId}/complete")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<CompleteQuestResponse>> completeQuest(
            Authentication authentication,
            @PathVariable("questId") Integer questId) {

        try {
            CompleteQuestResponse response = groupQuestService.completeQuest(questId);
            return ResponseEntity.ok(ApiResponse.success("단체 퀘스트가 완료되었습니다.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("GROUP_QUEST_NOT_FOUND", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("GROUP_QUEST_ALREADY_COMPLETED", e.getMessage()));
        }
    }

    @GetMapping("/{questId}/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentQuestDetailResponse>> getStudentQuestDetail(
            Authentication authentication,
            @PathVariable("questId") Integer questId) {

        Integer studentId = getStudentId(authentication);

        try {
            StudentQuestDetailResponse response = groupQuestService.getStudentQuestDetail(questId, studentId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("GROUP_QUEST_NOT_FOUND", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("INVALID_QUEST_STATUS", e.getMessage()));
        }
    }

    // Helper methods
    private Integer getTeacherId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getMemberId();
    }

    private Integer getStudentId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getMemberId();
    }
}
