package com.universite.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class UpdatePersonnelRequest {

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Boolean actif;
    private String fonction;
    private String service;
    private String grade;
    private String specialite;
    private String ine;
    private Integer anneeEntree;
    private Integer anneeSortie;
    private String tutorGroup;
    private String partnerZone;
    private Integer modulesManaged;
    private Integer studentsFollowed;
    private String program;
    private String promotion;

    @JsonAlias("password")
    private String motDePasse;
}
