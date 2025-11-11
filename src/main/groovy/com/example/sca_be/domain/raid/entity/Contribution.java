package com.example.sca_be.domain.raid.entity;

import com.example.sca_be.domain.auth.entity.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "contributions",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"raid_id", "student_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Contribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contribution_id")
    private Integer contributionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raid_id")
    private Raid raid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(nullable = false)
    private Integer damage = 0; // (Default: 0)

    @CreatedDate
    @Column(name = "updated_at", updatable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Contribution(Raid raid, Student student, Integer damage) {
        this.raid = raid;
        this.student = student;
        this.damage = (damage != null) ? damage : 0;
    }
}
