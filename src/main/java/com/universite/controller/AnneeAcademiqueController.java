package com.universite.controller;

import com.universite.dto.AnneeAcademiqueRequest;
import com.universite.dto.AnneeAcademiqueResponse;
import com.universite.service.AnneeAcademiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annees-academiques")
@RequiredArgsConstructor
public class AnneeAcademiqueController {

    private final AnneeAcademiqueService anneeAcademiqueService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR', 'ETUDIANT')")
    public List<AnneeAcademiqueResponse> getAll() {
        return anneeAcademiqueService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR', 'ETUDIANT')")
    public AnneeAcademiqueResponse getById(@PathVariable Long id) {
        return anneeAcademiqueService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public AnneeAcademiqueResponse create(@RequestBody AnneeAcademiqueRequest request) {
        return anneeAcademiqueService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public AnneeAcademiqueResponse update(
            @PathVariable Long id,
            @RequestBody AnneeAcademiqueRequest request
    ) {
        return anneeAcademiqueService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public void delete(@PathVariable Long id) {
        anneeAcademiqueService.delete(id);
    }
}
