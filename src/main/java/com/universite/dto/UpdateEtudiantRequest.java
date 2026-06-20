package com.universite.dto;

import lombok.Data;

@Data
public class UpdateEtudiantRequest {

    private String email;
    private String motDePasse;
    private String nom;
    private String prenom;
    private String telephone;
    private String ine;
    private String dateNaissance;
    private String niveau;
    private Long filiereId;
    private Long promotionId;
    private Long groupeEtudiantId;
}
