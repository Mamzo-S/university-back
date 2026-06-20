package com.universite.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest {

    private String email;

    @JsonAlias("password")
    private String motDePasse;
    private String nom;
    private String prenom;
    private String telephone;

    /** Un ou plusieurs rôles : ADMIN, FORMATEUR, ETUDIANT, PERSONNEL_ADMIN, TUTEUR, etc. */
    private List<String> roles;

    /** Rétrocompatibilité si roles est vide */
    private String role;

    // Profil étudiant
    private String ine;
    private String dateNaissance;
    private Long promotionId;
    private String promotionNom;
    private String formationNom;
    private String niveau;

    // Profil formateur (optionnel)
    private String grade;
    private String specialite;

    // Profil personnel (optionnel)
    private String fonction;
    private String service;
    private String typePersonnel;
}
