package com.example.sca_be.domain.raid.repository;

import com.example.sca_be.domain.raid.entity.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ContributionRepository extends JpaRepository<Contribution, Integer> {
}
