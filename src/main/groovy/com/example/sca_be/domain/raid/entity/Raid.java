package com.example.sca_be.domain.raid.entity;

import com.example.sca_be.domain.auth.entity.Member;
import com.example.sca_be.domain.classroom.entity.Classes;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "raids")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Raid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raid_id")
    private Integer raidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Member teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Classes classes;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "total_boss_hp")
    private Long totalBossHp;

    @Column(name = "current_boss_hp")
    private Long currentBossHp;

    @Column(name = "reward_coral")
    private Integer rewardCoral;

    @Lob
    @Column(name = "special_reward_description")
    private String specialRewardDescription;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RaidStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "boss_type", length = 30)
    private RaidTemplate bossType;

    @OneToMany(mappedBy = "raid", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contribution> contributions = new ArrayList<>();

    @Builder
    public Raid(Member teacher, Classes classes, LocalDateTime startDate, LocalDateTime endDate, Long totalBossHp, Long currentBossHp, Integer rewardCoral, String specialRewardDescription, RaidStatus status, Difficulty difficulty, RaidTemplate bossType) {
        this.teacher = teacher;
        this.classes = classes;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalBossHp = totalBossHp;
        this.currentBossHp = currentBossHp;
        this.rewardCoral = rewardCoral;
        this.specialRewardDescription = specialRewardDescription;
        this.status = status;
        this.difficulty = difficulty;
        this.bossType = bossType;
    }
}
