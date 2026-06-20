package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulletinSummaryResponse {

    private String semestre;
    private String anneeAcademique;
    private Double moyenneGenerale;
    private String mention;
    private String datePublication;
    private int nombreNotes;
}
