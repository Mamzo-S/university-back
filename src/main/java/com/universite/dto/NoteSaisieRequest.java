package com.universite.dto;

import lombok.Data;

@Data
public class NoteSaisieRequest {
    private Long etudiantId;
    private Long coursId;
    private String typeEvaluation;
    private Double valeur;
    private String anneeAcademique;
    private String semestre;
}
