package com.universite.dto;

import lombok.Data;

@Data
public class StageRequest {

    private Long etudiantId;
    private Long partenaireId;
    private String sujet;
    private String description;
    private String dateDebut;
    private String dateFin;
    private String statut;
    private String tuteurEntreprise;
    private String tuteurUniversite;
    private Boolean conventionSignee;
    private String commentaire;
}
