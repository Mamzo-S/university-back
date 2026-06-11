package com.universite.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtudiantDTO {

    private Long id;

    private String ine;

    private String nom;

    private String prenom;

    private LocalDate dateNaissance;

    private String genre;

    private Integer anneeDebut;

    private Integer anneeSortie;

    private Long formationId;

    private String formationNom;
}