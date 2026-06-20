package com.universite.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulletinResponse {
    private Long etudiantId;
    private String etudiantNomComplet;
    private String etudiantIne;
    private String filiereNom;
    private String semestre;
    private String anneeAcademique;
    private Double moyenneGenerale;
    private String mention;
    private String datePublication;
    private List<CoursBulletinLine> lignesCours;
    private List<NoteResponse> notes;
}
