package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FiliereDetailResponse {

    private Long id;
    private String nom;
    private String description;
    private long moduleCount;
    private long etudiantCount;
    private List<FiliereModuleSummary> modules;
    private List<FiliereEtudiantSummary> etudiants;
}
