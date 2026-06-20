package com.universite.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {

    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private Boolean actif;

    @JsonAlias("password")
    private String motDePasse;

    // Profil étudiant
    private String ine;
    private String dateNaissance;
    private Long promotionId;
    private String promotionNom;
    private String formationNom;
    private String niveau;

    // Profil formateur
    private String grade;
    private String specialite;

    // Profil personnel
    private String fonction;
    private String service;
}
