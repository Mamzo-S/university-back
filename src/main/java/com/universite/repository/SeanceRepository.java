package com.universite.repository;

import com.universite.entity.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeanceRepository extends JpaRepository<Seance, Long> {

    List<Seance> findByEmploiDuTempsId(Long emploiDuTempsId);

    List<Seance> findByEmploiDuTemps_Promotion_Id(Long promotionId);

    List<Seance> findByFormateurId(Long formateurId);

    List<Seance> findByEmploiDuTemps_Promotion_Formation_Id(Long formationId);

    /**
     * Séances dont le module est accessible à l'étudiant :
     * même filière + niveau que le module, ou module rattaché à sa promotion.
     * (Inverse de {@link EtudiantRepository#findByModuleScope}.)
     */
    @Query("""
            SELECT DISTINCT s FROM Seance s
            JOIN FETCH s.cours c
            JOIN FETCH c.formation f
            LEFT JOIN FETCH f.filiere ff
            JOIN FETCH s.formateur fm
            JOIN FETCH fm.utilisateur
            JOIN FETCH s.emploiDuTemps e
            JOIN FETCH e.promotion ep
            WHERE (
                :promotionFormationId IS NOT NULL
                AND f.id = :promotionFormationId
            ) OR (
                :filiereId IS NOT NULL
                AND :niveau IS NOT NULL
                AND f.niveau = :niveau
                AND ff.id = :filiereId
            )
            """)
    List<Seance> findVisibleForEtudiant(
            @Param("filiereId") Long filiereId,
            @Param("niveau") com.universite.entity.NiveauEtude niveau,
            @Param("promotionFormationId") Long promotionFormationId
    );
}
