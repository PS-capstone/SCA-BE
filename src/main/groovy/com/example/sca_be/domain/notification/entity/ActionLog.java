package com.example.sca_be.domain.notification.entity;

import com.example.sca_be.domain.auth.entity.Member;
import com.example.sca_be.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ActionLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Member student;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 50)
    private ActionLogType actionLogType;

    @Column(name = "change_coral")
    private Integer changeCoral;

    @Column(name = "change_research")
    private Integer changeResearch;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "log_message", length = 255)
    private String logMessage;

    @Builder
    public ActionLog(Member student, ActionLogType actionLogType, Integer changeCoral, Integer changeResearch, Integer referenceId, String logMessage) {
        this.student = student;
        this.actionLogType = actionLogType;
        this.changeCoral = changeCoral;
        this.changeResearch = changeResearch;
        this.referenceId = referenceId;
        this.logMessage = logMessage;
    }
}
