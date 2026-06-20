package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FiliereEtudiantSummary {

    private Long id;
    private String prenom;
    private String nom;
    private String email;
    private String ine;
    private String promotionNom;
    private String formationNom;
}
