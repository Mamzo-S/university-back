package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromotionResponse {

    private Long id;
    private String titre;
    private String nom;
    private String slug;
    private String description;
    private String anneeAcademique;
    private Long anneeAcademiqueId;
    private String anneeAcademiqueTitre;
    private Long formationId;
    private String formationNom;
    private long effectif;
}
