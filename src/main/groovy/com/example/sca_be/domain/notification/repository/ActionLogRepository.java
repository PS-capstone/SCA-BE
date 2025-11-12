package com.example.sca_be.domain.notification.repository;

import com.example.sca_be.domain.notification.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
}
