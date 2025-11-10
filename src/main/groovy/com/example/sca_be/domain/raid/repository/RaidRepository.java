package com.example.sca_be.domain.raid.repository;

import com.example.sca_be.domain.raid.entity.Raid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RaidRepository extends JpaRepository<Raid, Integer> {
}