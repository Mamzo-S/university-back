package com.universite.controller;

import com.universite.dto.GroupeEtudiantRequest;
import com.universite.dto.GroupeEtudiantResponse;
import com.universite.service.GroupeEtudiantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groupes-etudiants")
@RequiredArgsConstructor
public class GroupeEtudiantController {

    private final GroupeEtudiantService groupeEtudiantService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public List<GroupeEtudiantResponse> listAll(
            @RequestParam(required = false) Long promotionId,
            @RequestParam(required = false) Long formationId,
            @RequestParam(required = false) Long filiereId
    ) {
        return groupeEtudiantService.listAll(promotionId, formationId, filiereId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public GroupeEtudiantResponse getById(@PathVariable Long id) {
        return groupeEtudiantService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public GroupeEtudiantResponse create(@RequestBody GroupeEtudiantRequest request) {
        return groupeEtudiantService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public GroupeEtudiantResponse update(
            @PathVariable Long id,
            @RequestBody GroupeEtudiantRequest request
    ) {
        return groupeEtudiantService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public void delete(@PathVariable Long id) {
        groupeEtudiantService.delete(id);
    }
}
