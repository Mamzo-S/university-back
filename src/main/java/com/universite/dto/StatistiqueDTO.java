package com.universite.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatistiqueDTO {

    private long totalEtudiants;

    private long totalFormations;

    private long totalHommes;

    private long totalFemmes;

    private long totalFormateurs;

    private long totalPersonnels;
}
