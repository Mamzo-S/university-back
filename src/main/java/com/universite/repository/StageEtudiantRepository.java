package com.universite.repository;

import com.universite.entity.Etudiant;
import com.universite.entity.Partenaire;
import com.universite.entity.StageEtudiant;
import com.universite.entity.StatutStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StageEtudiantRepository extends JpaRepository<StageEtudiant, Long> {

    @Query("""
            SELECT s FROM StageEtudiant s
            JOIN FETCH s.etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.filiere
            JOIN FETCH s.partenaire
            ORDER BY s.dateDebut DESC NULLS LAST, s.id DESC
            """)
    List<StageEtudiant> findAllWithDetails();

    @Query("""
            SELECT s FROM StageEtudiant s
            JOIN FETCH s.etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.filiere
            JOIN FETCH s.partenaire
            WHERE e.utilisateur.email = :email
            ORDER BY s.dateDebut DESC NULLS LAST, s.id DESC
            """)
    List<StageEtudiant> findByEtudiantEmail(@Param("email") String email);

    long countByStatut(StatutStage statut);

    long countByConventionSigneeFalse();

    long countByPartenaire(Partenaire partenaire);

    List<StageEtudiant> findByEtudiant(Etudiant etudiant);
}
