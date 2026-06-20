package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agrégat emploi du temps : une promotion (classe) et ses séances planifiées.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmploiDuTempsResponse {

    private Long id;
    private Long promotionId;
    private String promotionNom;
    private String anneeAcademique;
    private Long formationId;
    private String formationNom;
    private String libelle;
    private boolean publie;
    private long effectif;
    private List<SeanceResponse> seances;
}
