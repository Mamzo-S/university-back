package com.universite.dto;

import lombok.Data;

@Data
public class PartenaireRequest {

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
}
