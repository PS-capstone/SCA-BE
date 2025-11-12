package com.example.sca_be.domain.personalquest.repository;

import com.example.sca_be.domain.personalquest.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

}