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
public class EtudiantFiliereView {

    private Long id;
    private String nom;
    private String description;
    private String niveauEtudiant;
    private List<FiliereModuleSummary> modules;
}
