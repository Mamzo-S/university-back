package com.universite.repository;

import com.universite.entity.Formation;
import com.universite.entity.NiveauEtude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FormationRepository extends JpaRepository<Formation, Long> {

    Optional<Formation> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<Formation> findByFiliereId(Long filiereId);

    List<Formation> findByFiliereIdAndNiveau(Long filiereId, NiveauEtude niveau);

    @Query("""
            SELECT DISTINCT f FROM Formation f
            LEFT JOIN FETCH f.filiere
            ORDER BY f.titre, f.nom
            """)
    List<Formation> findAllWithFiliere();

    long countByFiliereId(Long filiereId);
}