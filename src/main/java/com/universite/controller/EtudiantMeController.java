package com.universite.controller;

import com.universite.dto.EtudiantFiliereView;
import com.universite.dto.SeanceResponse;
import com.universite.service.EmploiDuTempsService;
import com.universite.service.EtudiantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Routes « moi » de l'étudiant connecté (JWT).
 * Contrôleur dédié pour éviter les conflits avec /api/etudiants/{id}.
 */
@RestController
@RequestMapping("/api/etudiants/me")
@RequiredArgsConstructor
public class EtudiantMeController {

    private final EtudiantService etudiantService;
    private final EmploiDuTempsService emploiDuTempsService;

    @GetMapping("/filiere")
    @PreAuthorize("hasAuthority('ETUDIANT')")
    public EtudiantFiliereView myFiliere(Authentication authentication) {
        return etudiantService.getMyFiliereView(authentication.getName());
    }

    /** Séances des modules (filière + niveau de l'étudiant). */
    @GetMapping("/seances")
    @PreAuthorize("hasAuthority('ETUDIANT')")
    public List<SeanceResponse> mySeances(Authentication authentication) {
        return emploiDuTempsService.getForCurrentEtudiant(authentication.getName());
    }
}
