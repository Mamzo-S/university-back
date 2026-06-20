package com.universite.repository;

import com.universite.entity.AnneeAcademique;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnneeAcademiqueRepository extends JpaRepository<AnneeAcademique, Long> {

    Optional<AnneeAcademique> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
