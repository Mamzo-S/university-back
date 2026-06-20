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
public class TrainingFormationSummary {

    private Long id;
    private String titre;
    private String niveau;
    private String typeFormation;
    private String filiereNom;
    private long effectif;
    private List<String> formateurNoms;
}
