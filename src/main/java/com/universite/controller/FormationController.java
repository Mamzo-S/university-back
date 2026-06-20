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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public FormationResponse create(@RequestBody FormationRequest request) {
        return formationService.create(request);
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public FormationParcoursDto updateParcours(
            @PathVariable Long id,
            @RequestBody FormationParcoursDto parcours
    ) {
        return formationParcoursService.updateParcours(id, parcours);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public FormationResponse update(@PathVariable Long id, @RequestBody FormationRequest request) {
        return formationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public void delete(@PathVariable Long id) {
        formationService.delete(id);
    }
}
