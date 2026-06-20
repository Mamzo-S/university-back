package com.universite.controller;

import com.universite.dto.PromotionRequest;
import com.universite.dto.PromotionResponse;
import com.universite.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR', 'ETUDIANT')")
    public List<PromotionResponse> list(
            @RequestParam(required = false) Long formationId
    ) {
        return promotionService.listAll(formationId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR', 'ETUDIANT')")
    public PromotionResponse getById(@PathVariable Long id) {
        return promotionService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public PromotionResponse create(@RequestBody PromotionRequest request) {
        return promotionService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public PromotionResponse update(@PathVariable Long id, @RequestBody PromotionRequest request) {
        return promotionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public void delete(@PathVariable Long id) {
        promotionService.delete(id);
    }
}
