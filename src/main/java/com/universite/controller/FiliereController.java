package com.universite.controller;

import com.universite.dto.FiliereRequest;
import com.universite.dto.FiliereResponse;
import com.universite.service.FiliereService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filieres")
@RequiredArgsConstructor
public class FiliereController {

    private final FiliereService filiereService;

    @PostMapping
    public FiliereResponse create(
            @RequestBody FiliereRequest request
    ) {
        return filiereService.create(request);
    }

    @GetMapping
    public List<FiliereResponse> getAll() {
        return filiereService.getAll();
    }

    @GetMapping("/{id}")
    public FiliereResponse getById(
            @PathVariable Long id
    ) {
        return filiereService.getById(id);
    }

    @PutMapping("/{id}")
    public FiliereResponse update(
            @PathVariable Long id,
            @RequestBody FiliereRequest request
    ) {
        return filiereService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) {
        filiereService.delete(id);
    }
}