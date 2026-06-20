package com.universite.repository;

import com.universite.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByNomIgnoreCase(String nom);

    List<Promotion> findByFormationId(Long formationId);

    List<Promotion> findByAnneeAcademiqueRefId(Long anneeAcademiqueId);

    Optional<Promotion> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
