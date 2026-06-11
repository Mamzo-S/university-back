package com.universite.controller;

import com.universite.dto.EtudiantDTO;
import com.universite.entity.Etudiant;
import com.universite.service.EtudiantService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
@RequiredArgsConstructor
public class EtudiantController {

    private final EtudiantService etudiantService;

    // =====================================
    // AJOUTER ETUDIANT
    // =====================================

    @PostMapping
    public Etudiant ajouterEtudiant(
            @Valid @RequestBody Etudiant etudiant
    ) {

        return etudiantService
                .ajouterEtudiant(etudiant);
    }

    // =====================================
    // LISTE COMPLETE
    // =====================================

    @GetMapping
    public List<Etudiant> getAllEtudiants() {

        return etudiantService
                .getAllEtudiants();
    }

    // =====================================
    // ETUDIANT PAR ID
    // =====================================

    @GetMapping("/{id}")
    public Etudiant getEtudiantById(
            @PathVariable Long id
    ) {

        return etudiantService
                .getEtudiantById(id);
    }

    // =====================================
    // MODIFIER ETUDIANT
    // =====================================

    @PutMapping("/{id}")
    public Etudiant modifierEtudiant(

            @PathVariable Long id,

            @RequestBody Etudiant etudiant
    ) {

        return etudiantService
                .modifierEtudiant(id, etudiant);
    }

    // =====================================
    // SUPPRIMER ETUDIANT
    // =====================================

    @DeleteMapping("/{id}")
    public void supprimerEtudiant(
            @PathVariable Long id
    ) {

        etudiantService
                .supprimerEtudiant(id);
    }

    // =====================================
    // PAGINATION
    // =====================================

    @GetMapping("/paginated")
    public Page<EtudiantDTO> getEtudiantsPagines(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "5")
            int size
    ) {

        return etudiantService
                .getEtudiantsPagines(
                        page,
                        size
                );
    }

    // =====================================
    // RECHERCHE PAR NOM
    // =====================================

    @GetMapping("/search")
    public Page<EtudiantDTO> rechercherParNom(

            @RequestParam String nom,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "5")
            int size
    ) {

        return etudiantService
                .rechercherParNom(
                        nom,
                        page,
                        size
                );
    }

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportExcel() {

        ByteArrayInputStream file =
                etudiantService.exportExcel();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=etudiants.xlsx"
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(
                        new InputStreamResource(file)
                );
    }
}