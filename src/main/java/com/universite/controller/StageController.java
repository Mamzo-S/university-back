package com.universite.controller;

import com.universite.dto.StageRequest;
import com.universite.dto.StageResponse;
import com.universite.service.StageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stages")
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public List<StageResponse> getAll() {
        return stageService.getAll();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ETUDIANT')")
    public List<StageResponse> myStages(Authentication authentication) {
        return stageService.getForCurrentEtudiant(authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public StageResponse getById(@PathVariable Long id) {
        return stageService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public StageResponse create(@RequestBody StageRequest request) {
        return stageService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public StageResponse update(
            @PathVariable Long id,
            @RequestBody StageRequest request
    ) {
        return stageService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public void delete(@PathVariable Long id) {
        stageService.delete(id);
    }
}
