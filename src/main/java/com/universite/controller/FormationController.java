package com.universite.controller;

import com.universite.dto.FormationRequest;
import com.universite.dto.FormationResponse;
import com.universite.service.FormationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
public class FormationController {

    private final FormationService formationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public FormationResponse create(@RequestBody FormationRequest request) {
        return formationService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATEUR', 'RESPONSABLE_FORMATION', 'ETUDIANT')")
    public List<FormationResponse> getAll() {
        return formationService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATEUR', 'RESPONSABLE_FORMATION', 'ETUDIANT')")
    public FormationResponse getById(@PathVariable Long id) {
        return formationService.getById(id);
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
