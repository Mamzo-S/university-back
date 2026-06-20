package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoursBulletinLine {

    private Long coursId;
    private String coursCode;
    private String coursNom;
    private Double coefficient;
    private Double moyenneCours;
}
