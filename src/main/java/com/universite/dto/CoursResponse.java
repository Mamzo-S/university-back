package com.universite.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoursResponse {
    private Long id;
    private String code;
    private String nom;
    private String semestre;
    private Double coefficient;
    private Long formationId;
    private String formationNom;
}
