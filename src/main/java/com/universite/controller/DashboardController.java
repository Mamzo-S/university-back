package com.universite.controller;

import com.universite.dto.AdminDashboardResponse;
import com.universite.dto.CareerStatsResponse;
import com.universite.dto.StatistiqueDTO;
import com.universite.dto.TrainingDashboardResponse;
import com.universite.service.DashboardService;
import com.universite.service.StageService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final StageService stageService;

    @GetMapping("/statistiques")
    @PreAuthorize("hasAuthority('ADMIN')")
    public StatistiqueDTO getStatistiques() {
        return dashboardService.getStatistiques();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AdminDashboardResponse getAdminStats() {
        return dashboardService.getAdminStats();
    }

    @GetMapping("/training")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public TrainingDashboardResponse getTrainingStats() {
        return dashboardService.getTrainingStats();
    }

    @GetMapping("/career")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SERVICE_INSERTION')")
    public CareerStatsResponse getCareerStats() {
        return stageService.getCareerStats();
    }
}
