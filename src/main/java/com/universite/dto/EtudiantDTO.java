package com.universite.dto;

import com.universite.entity.NiveauEtude;
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

    private Long utilisateurId;

    private String nom;

    private String prenom;

    private String email;

    private LocalDate dateNaissance;

    private NiveauEtude niveau;

    private Long promotionId;

    private String promotionNom;

    private Long formationId;

    private String formationNom;

    private Long filiereId;

    private String filiereNom;

    private Long groupeEtudiantId;

    private String groupeEtudiantNom;
}
