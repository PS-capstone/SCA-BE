package com.example.sca_be.domain.auth.repository;

import com.example.sca_be.domain.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    // 필요한 메서드(e.g., findByUsername)는 나중에 API 개발 시 추가 예정
}
