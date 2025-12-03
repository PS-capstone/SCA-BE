package com.example.sca_be.domain.gacha.repository;

import com.example.sca_be.domain.gacha.entity.Fish;
import com.example.sca_be.domain.gacha.entity.FishGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FishRepository extends JpaRepository<Fish, Integer> {
    // grade 컬럼에 인덱스가 걸려있어 효율적인 조회 가능
    List<Fish> findByGrade(FishGrade grade);
    
    // fish_id로 정렬하여 조회 (MySQL과 H2 데이터 순서 일치 보장)
    List<Fish> findAllByOrderByFishIdAsc();
}
