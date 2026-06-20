package com.universite.controller;

import com.universite.dto.EmploiDuTempsResponse;
import com.universite.dto.SeanceRequest;
import com.universite.dto.SeanceResponse;
import com.universite.entity.RoleName;
import com.universite.entity.Utilisateur;
import com.universite.repository.UtilisateurRepository;
import com.universite.service.EmploiDuTempsService;
import com.universite.service.SeanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emplois-du-temps")
@RequiredArgsConstructor
public class EmploiDuTempsController {

    private final EmploiDuTempsService emploiDuTempsService;
    private final SeanceService seanceService;
    private final UtilisateurRepository utilisateurRepository;

    /** Agrégat : emploi du temps complet d'une promotion (classe). */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR', 'ETUDIANT')")
    public EmploiDuTempsResponse getByPromotion(@RequestParam Long promotionId) {
        return emploiDuTempsService.getByPromotion(promotionId);
    }

    /** EDT personnel selon le rôle connecté. */
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('FORMATEUR', 'ETUDIANT')")
    public Object getForMe(Authentication authentication) {
        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (utilisateur.hasRole(RoleName.ETUDIANT)) {
            return emploiDuTempsService.getForCurrentEtudiant(email);
        }
        if (utilisateur.hasRole(RoleName.FORMATEUR)) {
            return emploiDuTempsService.getSeancesForCurrentFormateur(email);
        }

        throw new RuntimeException("Consultation de l'emploi du temps non disponible pour ce rôle");
    }

    @PostMapping("/seances")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public SeanceResponse createSeance(@RequestBody SeanceRequest request) {
        return seanceService.create(request);
    }

    @PutMapping("/seances/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public SeanceResponse updateSeance(@PathVariable Long id, @RequestBody SeanceRequest request) {
        return seanceService.update(id, request);
    }

    @DeleteMapping("/seances/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public void deleteSeance(@PathVariable Long id) {
        seanceService.delete(id);
    }

    /** Liste des séances : toutes si promotionId absent (admin / resp. formation), sinon par promotion. */
    @GetMapping("/seances")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR', 'ETUDIANT')")
    public List<SeanceResponse> listSeances(
            @RequestParam(required = false) Long promotionId,
            Authentication authentication
    ) {
        if (promotionId != null) {
            return seanceService.listByPromotion(promotionId);
        }

        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (utilisateur.hasRole(RoleName.ADMIN) || utilisateur.hasRole(RoleName.RESPONSABLE_FORMATION)) {
            return seanceService.listAll();
        }

        throw new RuntimeException("La promotion est requise pour consulter les séances");
    }

    /** Rétrocompatibilité POST/PUT/DELETE sur la racine. */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public SeanceResponse createSeanceLegacy(@RequestBody SeanceRequest request) {
        return seanceService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public SeanceResponse updateSeanceLegacy(@PathVariable Long id, @RequestBody SeanceRequest request) {
        return seanceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public void deleteSeanceLegacy(@PathVariable Long id) {
        seanceService.delete(id);
    }
}
