package com.universite.repository;

import com.universite.entity.Formateur;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FormateurRepository extends JpaRepository<Formateur, Long> {

    Optional<Formateur> findByUtilisateur_Id(Long utilisateurId);

    Optional<Formateur> findByUtilisateur_Email(String email);

    @EntityGraph(attributePaths = {"formations", "formations.filiere", "utilisateur"})
    Optional<Formateur> findWithFormationsByUtilisateur_Email(String email);

    @EntityGraph(attributePaths = {"formations", "utilisateur"})
    @Query("SELECT f FROM Formateur f")
    List<Formateur> findAllWithFormations();
}
