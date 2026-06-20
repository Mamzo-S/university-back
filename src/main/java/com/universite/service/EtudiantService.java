package com.universite.service;

import com.universite.dto.CreateEtudiantRequest;
import com.universite.dto.EtudiantDTO;
import com.universite.dto.EtudiantFiliereView;
import com.universite.dto.MembreResponse;
import com.universite.dto.UpdateEtudiantRequest;
import com.universite.entity.Etudiant;
import org.springframework.data.domain.Page;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface EtudiantService {

    Etudiant ajouterEtudiant(Etudiant etudiant);

    MembreResponse createEtudiant(CreateEtudiantRequest request, String creatorEmail);

    List<MembreResponse> listEtudiantsForUser(String userEmail);

    List<MembreResponse> listEtudiantsByModuleForFormateur(String userEmail, Long moduleId);

    List<MembreResponse> listEtudiantsByModule(Long moduleId);

    List<MembreResponse> listEtudiantsByModuleForUser(String userEmail, Long moduleId);

    List<MembreResponse> listEtudiantsFiltered(
            String userEmail,
            Long filiereId,
            Long formationId,
            Long promotionId,
            Long groupeEtudiantId
    );

    MembreResponse updateEtudiant(Long id, UpdateEtudiantRequest request);

    EtudiantFiliereView getMyFiliereView(String userEmail);

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
