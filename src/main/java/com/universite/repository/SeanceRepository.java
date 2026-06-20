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

    @Query("""
            SELECT s FROM Seance s
            JOIN FETCH s.cours c
            JOIN FETCH c.formation f
            LEFT JOIN FETCH f.filiere
            JOIN FETCH s.formateur fm
            JOIN FETCH fm.utilisateur
            JOIN FETCH s.emploiDuTemps e
            JOIN FETCH e.promotion
            WHERE s.jourSemaine = :jourSemaine
              AND f.filiere.id = :filiereId
              AND f.niveau = :niveau
            """)
    List<Seance> findByFiliereAndNiveauAndJour(
            @Param("filiereId") Long filiereId,
            @Param("niveau") com.universite.entity.NiveauEtude niveau,
            @Param("jourSemaine") com.universite.entity.JourSemaine jourSemaine
    );

    List<Seance> findByEmploiDuTemps_Promotion_Formation_Id(Long formationId);

    /**
     * Séances visibles pour l'étudiant : toujours dans sa filière,
     * au bon niveau ou module explicite de sa promotion (même filière).
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
            WHERE ff.id = :filiereId
              AND (
                  f.niveau = :niveau
                  OR (
                      :promotionFormationId IS NOT NULL
                      AND f.id = :promotionFormationId
                  )
              )
            """)
    List<Seance> findVisibleForEtudiant(
            @Param("filiereId") Long filiereId,
            @Param("niveau") com.universite.entity.NiveauEtude niveau,
            @Param("promotionFormationId") Long promotionFormationId
    );
}
