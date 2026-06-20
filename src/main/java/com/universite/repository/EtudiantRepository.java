package com.universite.repository;

import com.universite.entity.Etudiant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    Optional<Etudiant> findByIne(String ine);

    Optional<Etudiant> findByUtilisateur_Id(Long utilisateurId);

    Optional<Etudiant> findByUtilisateur_Email(String email);

    @Query("""
            SELECT e FROM Etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.filiere
            LEFT JOIN FETCH e.promotion p
            LEFT JOIN FETCH p.formation pf
            LEFT JOIN FETCH pf.filiere
            WHERE e.utilisateur.email = :email
            """)
    Optional<Etudiant> findByUtilisateur_EmailWithProfile(@Param("email") String email);

    Page<Etudiant> findByUtilisateur_NomContainingIgnoreCase(
            String nom,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.filiere
            LEFT JOIN FETCH e.groupeEtudiant
            LEFT JOIN FETCH e.promotion p
            LEFT JOIN FETCH p.formation f
            WHERE COALESCE(e.filiere.id, f.filiere.id) = :filiereId
            """)
    List<Etudiant> findByFiliereId(@Param("filiereId") Long filiereId);

    @Query("""
            SELECT e FROM Etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.filiere
            LEFT JOIN FETCH e.groupeEtudiant
            LEFT JOIN FETCH e.promotion p
            LEFT JOIN FETCH p.formation f
            WHERE e.promotion.id = :promotionId
            ORDER BY e.id
            """)
    List<Etudiant> findByPromotionId(@Param("promotionId") Long promotionId);

    @Query("""
            SELECT e FROM Etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.filiere
            LEFT JOIN FETCH e.groupeEtudiant
            LEFT JOIN FETCH e.promotion p
            LEFT JOIN FETCH p.formation f
            WHERE e.groupeEtudiant.id = :groupeEtudiantId
            ORDER BY e.id
            """)
    List<Etudiant> findByGroupeEtudiantId(@Param("groupeEtudiantId") Long groupeEtudiantId);

    @Query("""
            SELECT DISTINCT e FROM Etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.filiere
            LEFT JOIN FETCH e.groupeEtudiant
            LEFT JOIN FETCH e.promotion p
            LEFT JOIN FETCH p.formation f
            ORDER BY e.id
            """)
    List<Etudiant> findAllWithDetails();

    @Query("""
            SELECT DISTINCT e FROM Etudiant e
            JOIN FETCH e.utilisateur u
            LEFT JOIN FETCH e.filiere ef
            LEFT JOIN FETCH e.groupeEtudiant
            LEFT JOIN FETCH e.promotion p
            LEFT JOIN FETCH p.formation pf
            LEFT JOIN FETCH pf.filiere pff
            WHERE p.formation.id = :moduleId
               OR (
                   :filiereId IS NOT NULL
                   AND :niveau IS NOT NULL
                   AND e.niveau = :niveau
                   AND COALESCE(ef.id, pff.id) = :filiereId
               )
            ORDER BY u.nom, e.id
            """)
    List<Etudiant> findByModuleScope(
            @Param("moduleId") Long moduleId,
            @Param("filiereId") Long filiereId,
            @Param("niveau") com.universite.entity.NiveauEtude niveau
    );

    @Query("""
            SELECT COUNT(DISTINCT e) FROM Etudiant e
            LEFT JOIN e.filiere ef
            LEFT JOIN e.promotion p
            LEFT JOIN p.formation pf
            WHERE COALESCE(ef.id, pf.filiere.id) = :filiereId
            """)
    long countByFiliereId(@Param("filiereId") Long filiereId);

    @Query("""
            SELECT DISTINCT e FROM Etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.filiere
            LEFT JOIN FETCH e.groupeEtudiant
            LEFT JOIN FETCH e.promotion p
            LEFT JOIN FETCH p.formation f
            WHERE COALESCE(e.filiere.id, f.filiere.id) IN :filiereIds
            ORDER BY e.id
            """)
    List<Etudiant> findByFiliereIds(@Param("filiereIds") List<Long> filiereIds);

    long countByFiliere_Id(Long filiereId);

    @Query("""
            SELECT DISTINCT e FROM Etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.promotion p
            LEFT JOIN FETCH p.formation f
            WHERE f.id IN :formationIds
            ORDER BY e.id
            """)
    List<Etudiant> findByFormationIds(@Param("formationIds") List<Long> formationIds);

    long countByPromotion_Formation_Filiere_Id(Long filiereId);

    @Query("""
            SELECT COUNT(e) FROM Etudiant e
            WHERE e.promotion.formation.id = :formationId
            """)
    long countByFormationId(@Param("formationId") Long formationId);

    @Query("""
            SELECT COUNT(DISTINCT e) FROM Etudiant e
            LEFT JOIN e.filiere ef
            LEFT JOIN e.promotion p
            LEFT JOIN p.formation pf
            LEFT JOIN pf.filiere pff
            WHERE p.formation.id = :formationId
               OR (
                   :filiereId IS NOT NULL
                   AND :niveau IS NOT NULL
                   AND e.niveau = :niveau
                   AND COALESCE(ef.id, pff.id) = :filiereId
               )
            """)
    long countByFormationScope(
            @Param("formationId") Long formationId,
            @Param("filiereId") Long filiereId,
            @Param("niveau") com.universite.entity.NiveauEtude niveau
    );

    @Query("""
            SELECT DISTINCT e FROM Etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.filiere
            LEFT JOIN FETCH e.promotion p
            LEFT JOIN FETCH p.formation pf
            WHERE e.niveau = :niveau
            ORDER BY e.id
            """)
    List<Etudiant> findByNiveau(@Param("niveau") com.universite.entity.NiveauEtude niveau);

    @Query("""
            SELECT DISTINCT e FROM Etudiant e
            JOIN FETCH e.utilisateur
            LEFT JOIN FETCH e.filiere ef
            LEFT JOIN FETCH e.promotion p
            LEFT JOIN FETCH p.formation pf
            LEFT JOIN FETCH pf.filiere pff
            WHERE e.niveau = :niveau
              AND COALESCE(ef.id, pff.id) = :filiereId
            ORDER BY e.id
            """)
    List<Etudiant> findByFiliereIdAndNiveau(
            @Param("filiereId") Long filiereId,
            @Param("niveau") com.universite.entity.NiveauEtude niveau
    );
}
