package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StageResponse {

    private Long id;
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantEmail;
    private String etudiantIne;
    private String filiereNom;
    private Long partenaireId;
    private String partenaireNom;
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
