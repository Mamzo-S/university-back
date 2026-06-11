package com.universite.repository;

import com.universite.entity.Cours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoursRepository extends JpaRepository<Cours, Long> {
    Optional<Cours> findByCode(String code);
    List<Cours> findByFormationId(Long formationId);
}
