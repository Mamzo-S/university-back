package com.universite.repository;

import com.universite.entity.GroupeEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupeEtudiantRepository extends JpaRepository<GroupeEtudiant, Long> {

    List<GroupeEtudiant> findByPromotionId(Long promotionId);

    List<GroupeEtudiant> findByPromotionFormationId(Long formationId);

    List<GroupeEtudiant> findByPromotionFormationFiliereId(Long filiereId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
