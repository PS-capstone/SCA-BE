package com.example.sca_be.domain.gacha.repository;

import com.example.sca_be.domain.gacha.entity.Fish;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FishRepository extends JpaRepository<Fish, Integer> {
}
