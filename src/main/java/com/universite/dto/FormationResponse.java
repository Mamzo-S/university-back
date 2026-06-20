package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormationResponse {

    private Long id;
    private String titre;
    private String nom;
    private String slug;
    private String description;
    private String imageUrl;
    private String niveau;
    private String typeFormation;
    private String typeFinancement;
    private String dateDebut;
    private String dateFin;
    private BigDecimal montant;
    private Long filiereId;
    private String filiereNom;
    private List<String> formateurNoms;
}
