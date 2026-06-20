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
public class MembreResponse {

    private Long id;
    private Long utilisateurId;
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private boolean actif;
    private String role;
    private String fonction;
    private String service;
    private String grade;
    private String specialite;
    private String ine;
    private String dateNaissance;
    private String niveau;
    private String promotionNom;
    private String formationNom;
    private Long filiereId;
    private String filiereNom;
    private Long promotionId;
    private Long groupeEtudiantId;
    private String groupeEtudiantNom;
    private List<Long> formationIds;
    private List<String> formationNoms;
}
