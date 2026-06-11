package com.universite.controller;

import com.universite.dto.CoursRequest;
import com.universite.dto.CoursResponse;
import com.universite.service.CoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CoursController {

    private final CoursService coursService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESSEUR')")
    public CoursResponse create(@RequestBody CoursRequest request) {
        return coursService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESSEUR')")
    public CoursResponse update(@PathVariable Long id, @RequestBody CoursRequest request) {
        return coursService.update(id, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESSEUR', 'ETUDIANT')")
    public List<CoursResponse> getAll(
            @RequestParam(required = false) Long formationId
    ) {
        if (formationId != null) {
            return coursService.getByFormation(formationId);
        }
        return coursService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESSEUR', 'ETUDIANT')")
    public CoursResponse getById(@PathVariable Long id) {
        return coursService.getById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESSEUR')")
    public void delete(@PathVariable Long id) {
        coursService.delete(id);
    }
}
