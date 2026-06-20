package com.universite.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoteResponse {
    private Long id;
    private Long etudiantId;
    private String etudiantNomComplet;
    private Long coursId;
    private String coursCode;
    private String coursNom;
    private Double coursCoefficient;
    private String typeEvaluation;
    private Double valeur;
    private String anneeAcademique;
    private String semestre;
    private Boolean bulletinPublie;
}
