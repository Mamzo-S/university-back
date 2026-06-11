package com.universite.controller;

import com.universite.entity.Formation;
import com.universite.service.FormationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
public class FormationController {

    private final FormationService formationService;

    @PostMapping
    public Formation ajouterFormation(@RequestBody Formation formation) {
        return formationService.ajouterFormation(formation);
    }

    @GetMapping
    public List<Formation> getAllFormations() {
        return formationService.getAllFormations();
    }
}
