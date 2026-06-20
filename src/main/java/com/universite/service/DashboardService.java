package com.universite.service;

import com.universite.dto.AdminDashboardResponse;
import com.universite.dto.StatistiqueDTO;
import com.universite.dto.TrainingDashboardResponse;

public interface DashboardService {

    StatistiqueDTO getStatistiques();

    AdminDashboardResponse getAdminStats();

    TrainingDashboardResponse getTrainingStats();
}