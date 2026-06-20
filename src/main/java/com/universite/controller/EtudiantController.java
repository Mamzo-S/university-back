package com.universite.controller;

import com.universite.dto.CreateEtudiantRequest;
import com.universite.dto.EtudiantDTO;
import com.universite.dto.MembreResponse;
import com.universite.dto.SeanceResponse;
import com.universite.dto.UpdateEtudiantRequest;
import com.universite.entity.Etudiant;
import com.universite.service.EmploiDuTempsService;
import com.universite.service.EtudiantService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
@RequiredArgsConstructor
public class EtudiantController {

    private final EtudiantService etudiantService;
    private final EmploiDuTempsService emploiDuTempsService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR', 'SERVICE_INSERTION')")
    public List<MembreResponse> listEtudiants(
            Authentication authentication,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long filiereId,
            @RequestParam(required = false) Long formationId,
            @RequestParam(required = false) Long promotionId,
            @RequestParam(required = false) Long groupeEtudiantId
    ) {
        if (moduleId != null) {
            return etudiantService.listEtudiantsByModuleForUser(
                    authentication.getName(),
                    moduleId
            );
        }
        if (filiereId != null || formationId != null || promotionId != null || groupeEtudiantId != null) {
            return etudiantService.listEtudiantsFiltered(
                    authentication.getName(),
                    filiereId,
                    formationId,
                    promotionId,
                    groupeEtudiantId
            );
        }
        return etudiantService.listEtudiantsForUser(authentication.getName());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public MembreResponse createEtudiant(
            @Valid @RequestBody CreateEtudiantRequest request,
            Authentication authentication
    ) {
        return etudiantService.createEtudiant(request, authentication.getName());
    }

    @GetMapping("/{id:\\d+}/seances")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR', 'ETUDIANT')")
    public List<SeanceResponse> seancesForEtudiant(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return emploiDuTempsService.getForEtudiant(id, authentication.getName());
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public Etudiant getEtudiantById(@PathVariable Long id) {
        return etudiantService.getEtudiantById(id);
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public MembreResponse modifierEtudiant(
            @PathVariable Long id,
            @RequestBody UpdateEtudiantRequest request
    ) {
        return etudiantService.updateEtudiant(id, request);
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public void supprimerEtudiant(@PathVariable Long id) {
        etudiantService.supprimerEtudiant(id);
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public Page<EtudiantDTO> getEtudiantsPagines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return etudiantService.getEtudiantsPagines(page, size);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public Page<EtudiantDTO> rechercherParNom(
            @RequestParam String nom,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return etudiantService.rechercherParNom(nom, page, size);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public ResponseEntity<InputStreamResource> exportExcel() {
        ByteArrayInputStream file = etudiantService.exportExcel();

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
                .body(new InputStreamResource(file));
    }
}
