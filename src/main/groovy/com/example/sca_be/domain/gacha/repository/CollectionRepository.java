package com.example.sca_be.domain.gacha.repository;

import com.example.sca_be.domain.gacha.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

//CollectionEntry도 여기서 관리할거임
public interface CollectionRepository extends JpaRepository<Collection, Integer> {
}
