package com.universite.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulletinResponse {
    private Long etudiantId;
    private String etudiantNomComplet;
    private String semestre;
    private String anneeAcademique;
    private Double moyenneGenerale;
    private List<NoteResponse> notes;
}
