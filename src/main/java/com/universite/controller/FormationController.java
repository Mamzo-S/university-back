package com.universite.controller;

import com.universite.dto.FormationRequest;
import com.universite.dto.FormationResponse;
import com.universite.dto.parcours.FormationParcoursDto;
import com.universite.service.FormationService;
import com.universite.service.FormationParcoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
public class FormationController {

    private final FormationService formationService;
    private final FormationParcoursService formationParcoursService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public FormationResponse create(
            @RequestBody FormationRequest request,
            Authentication authentication
    ) {
        return formationService.create(request, authentication.getName());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<FormationResponse> getAll() {
        return formationService.getAll();
    }

    @GetMapping("/by-slug/{slug}")
    @PreAuthorize("isAuthenticated()")
    public FormationResponse getBySlug(
            @PathVariable String slug,
            Authentication authentication
    ) {
        return formationService.getBySlug(slug, authentication.getName());
    }

    @GetMapping("/by-slug/{slug}/parcours")
    @PreAuthorize("isAuthenticated()")
    public FormationParcoursDto getParcoursBySlug(
            @PathVariable String slug,
            Authentication authentication
    ) {
        return formationParcoursService.getParcoursBySlug(slug, authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public FormationResponse getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return formationService.getById(id, authentication.getName());
    }

    @GetMapping("/{id}/parcours")
    @PreAuthorize("isAuthenticated()")
    public FormationParcoursDto getParcours(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return formationParcoursService.getParcours(id, authentication.getName());
    }

    @PutMapping("/{id}/parcours")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public FormationParcoursDto updateParcours(
            @PathVariable Long id,
            @RequestBody FormationParcoursDto parcours,
            Authentication authentication
    ) {
        return formationParcoursService.updateParcours(id, parcours, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public FormationResponse update(
            @PathVariable Long id,
            @RequestBody FormationRequest request,
            Authentication authentication
    ) {
        return formationService.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public void delete(@PathVariable Long id, Authentication authentication) {
        formationService.delete(id, authentication.getName());
    }
}
