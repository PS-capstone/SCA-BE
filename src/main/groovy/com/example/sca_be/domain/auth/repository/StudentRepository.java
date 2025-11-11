package com.example.sca_be.domain.auth.repository;

import com.example.sca_be.domain.auth.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}
