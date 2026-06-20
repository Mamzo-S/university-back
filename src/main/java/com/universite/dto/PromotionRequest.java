package com.universite.dto;

import lombok.Data;

@Data
public class PromotionRequest {

    private String titre;
    /** Rétrocompatibilité */
    private String nom;
    private String slug;
    private String description;
    private String anneeAcademique;
    private Long anneeAcademiqueId;
}
