package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDashboardResponse {

    private long formations;
    private long schedules;
    private long trainers;
    private long students;
    private long filieres;
    private long promotions;
    private List<TrainingFormationSummary> formationSummaries;
}
