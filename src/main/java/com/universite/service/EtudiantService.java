package com.universite.service;

import com.universite.dto.EtudiantDTO;
import com.universite.entity.Etudiant;
import org.springframework.data.domain.Page;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface EtudiantService {

    Etudiant ajouterEtudiant(Etudiant etudiant);

    List<Etudiant> getAllEtudiants();

    Etudiant getEtudiantById(Long id);

    Etudiant modifierEtudiant(Long id, Etudiant etudiant);

    void supprimerEtudiant(Long id);

    Page<EtudiantDTO> rechercherParNom(
            String nom,
            int page,
            int size
    );

    Page<EtudiantDTO> getEtudiantsPagines(
            int page,
            int size
    );

    ByteArrayInputStream exportExcel();
}
