package com.universite.controller;

import com.universite.dto.StatistiqueDTO;
import com.universite.dto.CareerStatsResponse;
import com.universite.service.DashboardService;
import com.universite.service.StageService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final StageService stageService;

    @GetMapping("/statistiques")
    public StatistiqueDTO getStatistiques() {

        return dashboardService
                .getStatistiques();
    }

    @GetMapping("/career")
    public CareerStatsResponse getCareerStats() {
        return stageService.getCareerStats();
    }
}