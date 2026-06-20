package com.universite.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FormationRequest {

    private String titre;
    /** Rétrocompatibilité : utilisé si titre absent. */
    private String nom;
    private String slug;
    private String description;
    private String imageUrl;
    private String niveau;
    private String typeFormation;
    private String typeFinancement;
    private String dateDebut;
    private String dateFin;
    private BigDecimal montant;
    private Long filiereId;
    /** Métadonnées parcours initiales (création par formateur). */
    private String duration;
    private String sessionUrl;
}
