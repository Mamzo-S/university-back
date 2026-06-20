package com.universite.controller;

import com.universite.dto.FiliereDetailResponse;
import com.universite.dto.FiliereRequest;
import com.universite.dto.FiliereResponse;
import com.universite.service.FiliereService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filieres")
@RequiredArgsConstructor
public class FiliereController {

    private final FiliereService filiereService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public List<FiliereResponse> getAll() {
        return filiereService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public FiliereResponse getById(@PathVariable Long id) {
        return filiereService.getById(id);
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public FiliereDetailResponse getDetail(@PathVariable Long id) {
        return filiereService.getDetail(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public FiliereResponse create(@RequestBody FiliereRequest request) {
        return filiereService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public FiliereResponse update(
            @PathVariable Long id,
            @RequestBody FiliereRequest request
    ) {
        return filiereService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public void delete(@PathVariable Long id) {
        filiereService.delete(id);
    }
}
