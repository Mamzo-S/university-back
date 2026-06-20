package com.universite.controller;

import com.universite.dto.MembreResponse;
import com.universite.service.AdminMembreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/responsables-formation")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ResponsableFormationController {

    private final AdminMembreService adminMembreService;

    @GetMapping
    public List<MembreResponse> lister() {
        return adminMembreService.listResponsablesFormation();
    }
}
