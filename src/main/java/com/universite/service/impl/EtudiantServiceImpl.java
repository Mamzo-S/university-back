package com.universite.service.impl;

import com.universite.dto.EtudiantDTO;
import com.universite.entity.Etudiant;
import com.universite.export.ExcelExporter;
import com.universite.mapper.EtudiantMapper;
import com.universite.repository.EtudiantRepository;
import com.universite.service.EtudiantService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EtudiantServiceImpl
        implements EtudiantService {

    private final EtudiantRepository etudiantRepository;

    @Override
    public Etudiant ajouterEtudiant(
            Etudiant etudiant
    ) {

        if (etudiantRepository
                .findByIne(etudiant.getIne())
                .isPresent()) {

            throw new RuntimeException(
                    "INE déjà utilisé"
            );
        }

        return etudiantRepository.save(etudiant);
    }

    @Override
    public List<Etudiant> getAllEtudiants() {

        return etudiantRepository.findAll();
    }

    @Override
    public Etudiant getEtudiantById(Long id) {

        return etudiantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Etudiant introuvable"
                        )
                );
    }

    @Override
    public Etudiant modifierEtudiant(
            Long id,
            Etudiant etudiant
    ) {

        Etudiant existing =
                getEtudiantById(id);

        existing.setNom(etudiant.getNom());
        existing.setPrenom(etudiant.getPrenom());
        existing.setIne(etudiant.getIne());
        existing.setDateNaissance(
                etudiant.getDateNaissance()
        );
        existing.setGenre(etudiant.getGenre());
        existing.setAnneeDebut(
                etudiant.getAnneeDebut()
        );
        existing.setAnneeSortie(
                etudiant.getAnneeSortie()
        );
        existing.setFormation(
                etudiant.getFormation()
        );

        return etudiantRepository.save(existing);
    }

    @Override
    public void supprimerEtudiant(Long id) {

        Etudiant etudiant =
                getEtudiantById(id);

        etudiantRepository.delete(etudiant);
    }

    // =====================================
    // PAGINATION
    // =====================================

    @Override
    public Page<EtudiantDTO> getEtudiantsPagines(
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        return etudiantRepository
                .findAll(pageable)
                .map(EtudiantMapper::toDTO);
    }

    // =====================================
    // RECHERCHE PAR NOM
    // =====================================

    @Override
    public Page<EtudiantDTO> rechercherParNom(
            String nom,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        return etudiantRepository
                .findByNomContainingIgnoreCase(
                        nom,
                        pageable
                )
                .map(EtudiantMapper::toDTO);
    }

    @Override
    public ByteArrayInputStream exportExcel() {

        List<Etudiant> etudiants =
                etudiantRepository.findAll();

        return ExcelExporter
                .exportEtudiants(etudiants);
    }
}