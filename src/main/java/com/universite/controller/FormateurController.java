package com.universite.controller;

import com.universite.dto.FormateurFormationSummary;
import com.universite.dto.FormateurFormationsRequest;
import com.universite.dto.MembreResponse;
import com.universite.service.AdminMembreService;
import com.universite.service.FormateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formateurs")
@RequiredArgsConstructor
public class FormateurController {

    private final AdminMembreService adminMembreService;
    private final FormateurService formateurService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public List<MembreResponse> lister() {
        return adminMembreService.listFormateurs();
    }

    @GetMapping("/me/formations")
    @PreAuthorize("hasAuthority('FORMATEUR')")
    public List<FormateurFormationSummary> myFormations(Authentication authentication) {
        return formateurService.getFormationsForCurrentUser(authentication.getName());
    }

    @GetMapping("/me/modules")
    @PreAuthorize("hasAuthority('FORMATEUR')")
    public List<FormateurFormationSummary> myModules(Authentication authentication) {
        return formateurService.getModulesForCurrentUser(authentication.getName());
    }

    @GetMapping("/{id}/formations")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION', 'FORMATEUR')")
    public List<FormateurFormationSummary> getFormations(@PathVariable Long id) {
        return formateurService.getFormations(id);
    }

    @PutMapping("/{id}/formations")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public MembreResponse assignFormations(
            @PathVariable Long id,
            @RequestBody FormateurFormationsRequest request
    ) {
        return formateurService.assignFormations(id, request.getFormationIds());
    }
}
