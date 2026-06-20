package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupeEtudiantResponse {

    private Long id;
    private String titre;
    private String slug;
    private String description;
    private Long promotionId;
    private String promotionTitre;
    private Long formationId;
    private String formationNom;
    private Long filiereId;
    private String filiereNom;
    private long effectif;
}
