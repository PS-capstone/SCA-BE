package com.example.sca_be.domain.auth.entity;


import com.example.sca_be.domain.classroom.entity.Classes;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;


@Entity
@Table(name = "students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert//필드 누락을 확인하고 디폴트값 적용시키기 위해
public class Student {

    @Id
    @Column(name = "member_id")
    private Integer memberId;//student의 PK


    @OneToOne(fetch = FetchType.LAZY)
    @MapsId//부모 entity의 PK를 자식 Entity의 PK로 그대로 가져와 사용
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id") // 'unique = true' -> 1:1 관계 (학생 1명당 반 1개)
    private Classes classes;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer coral;

    @ColumnDefault("0")
    @Column(name = "research_data", nullable = false)
    private Integer researchData;

    @ColumnDefault("1.0")
    @Column(name = "correction_factor", nullable = false)
    private Float correctionFactor; // 보정계수

    @Builder
    public Student(Member member, Classes classes) {
        this.member = member;
        this.classes = classes;
    }

    // 보상 지급 메서드
    public void addCoral(Integer amount) {
        this.coral = (this.coral != null ? this.coral : 0) + amount;
    }

    public void addResearchData(Integer amount) {
        this.researchData = (this.researchData != null ? this.researchData : 0) + amount;
    }

    // Classes getter 메서드 이름 수정 (Service에서 사용)
    public Classes getClassEntity() {
        return this.classes;
    }
}
