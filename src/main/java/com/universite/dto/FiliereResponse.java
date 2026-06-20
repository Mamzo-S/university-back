package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FiliereResponse {

    private Long id;
    private String nom;
    private String description;
    private long moduleCount;
    private long etudiantCount;
}
