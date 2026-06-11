package com.universite.controller;

import com.universite.dto.StatistiqueDTO;
import com.universite.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistiques")
    public StatistiqueDTO getStatistiques() {

        return dashboardService
                .getStatistiques();
    }
}