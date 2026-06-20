package com.universite.repository;

import com.universite.entity.NiveauEtude;
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
     * Séances dont la formation (module) est dans la filière et le niveau donnés.
     */
    @Query("""
            SELECT DISTINCT s FROM Seance s
            JOIN FETCH s.cours c
            JOIN FETCH c.formation f
            JOIN FETCH s.formateur fm
            JOIN FETCH fm.utilisateur
            JOIN FETCH s.emploiDuTemps e
            JOIN FETCH e.promotion
            WHERE f.filiere.id = :filiereId
              AND f.niveau = :niveau
            """)
    List<Seance> findByFormationFiliereIdAndNiveau(
            @Param("filiereId") Long filiereId,
            @Param("niveau") NiveauEtude niveau
    );
}
