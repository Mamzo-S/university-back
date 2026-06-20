package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeanceResponse {

    private Long id;
    private Long emploiDuTempsId;
    private Long coursId;
    private String coursCode;
    private String coursNom;
    private Long formateurId;
    private String formateurNom;
    private Long promotionId;
    private String promotionNom;
    private Long formationId;
    private String formationNom;
    private Integer jourSemaine;
    private String heureDebut;
    private String heureFin;
    private String salle;
    private String typeSeance;
}
