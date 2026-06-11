package com.universite.repository;

import com.universite.entity.Etudiant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtudiantRepository
        extends JpaRepository<Etudiant, Long> {

    Optional<Etudiant> findByIne(String ine);

    Page<Etudiant> findByNomContainingIgnoreCase(
            String nom,
            Pageable pageable
    );
    long countByGenreIgnoreCase(String genre);

    List<Etudiant> findByNomAndPrenom(String nom, String prenom);
}
