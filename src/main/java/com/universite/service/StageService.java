package com.universite.service;

import com.universite.dto.CareerStatsResponse;
import com.universite.dto.StageRequest;
import com.universite.dto.StageResponse;

import java.util.List;

public interface StageService {

    List<StageResponse> getAll();

    List<StageResponse> getForCurrentEtudiant(String userEmail);

    StageResponse getById(Long id);

    StageResponse create(StageRequest request);

    StageResponse update(Long id, StageRequest request);

    void delete(Long id);

    CareerStatsResponse getCareerStats();
}
