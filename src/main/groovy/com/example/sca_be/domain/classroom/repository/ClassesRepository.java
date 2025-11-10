package com.example.sca_be.domain.classroom.repository;

import com.example.sca_be.domain.classroom.entity.Classes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassesRepository extends JpaRepository<Classes, Integer> {
}