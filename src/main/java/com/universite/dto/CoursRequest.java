package com.universite.dto;

import lombok.Data;

@Data
public class CoursRequest {
    private String code;
    private String nom;
    private String semestre;
    private Double coefficient;
    private Long formationId;
}
