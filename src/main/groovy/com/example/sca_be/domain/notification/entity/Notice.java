package com.example.sca_be.domain.notification.entity;
import com.example.sca_be.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", length = 50)
    private NoticeType noticeType;

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String content;

    @Builder
    public Notice(NoticeType noticeType, String title, String content) {
        this.noticeType = noticeType;
        this.title = title;
        this.content = content;
    }
}
