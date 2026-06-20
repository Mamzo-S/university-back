package com.universite.dto;

import lombok.Data;

@Data
public class SeanceRequest {

    /** Identifiant de l'emploi du temps (prioritaire si renseigné). */
    private Long emploiDuTempsId;

    /** Rétrocompatibilité : résout l'emploi du temps via la promotion (classe). */
    private Long promotionId;

    /** Module technique (cours) — optionnel si formationId est renseigné. */
    private Long coursId;

    /** Formation du catalogue — utilisée à la place du module dans l'emploi du temps. */
    private Long formationId;

    private Long formateurId;
    private Integer jourSemaine;
    private String heureDebut;
    private String heureFin;
    private String salle;
    private String typeSeance;
}
