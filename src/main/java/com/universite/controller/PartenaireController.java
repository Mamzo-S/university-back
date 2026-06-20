package com.universite.controller;

import com.universite.dto.PartenaireRequest;
import com.universite.dto.PartenaireResponse;
import com.universite.service.PartenaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partenaires")
@RequiredArgsConstructor
public class PartenaireController {

    private final PartenaireService partenaireService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public List<PartenaireResponse> getAll() {
        return partenaireService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public PartenaireResponse getById(@PathVariable Long id) {
        return partenaireService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public PartenaireResponse create(@RequestBody PartenaireRequest request) {
        return partenaireService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public PartenaireResponse update(
            @PathVariable Long id,
            @RequestBody PartenaireRequest request
    ) {
        return partenaireService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public void delete(@PathVariable Long id) {
        partenaireService.delete(id);
    }
}
