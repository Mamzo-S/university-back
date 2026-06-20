package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartenaireResponse {

    private Long id;
    private String nom;
    private String secteur;
    private String email;
    private String telephone;
    private String adresse;
    private String ville;
    private String pays;
    private String contactNom;
    private String contactFonction;
    private String description;
    private Boolean actif;
    private Boolean conventionCadre;
    private Long stageCount;
}
