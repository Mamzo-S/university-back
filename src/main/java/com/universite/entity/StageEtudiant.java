package com.universite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "stages_etudiants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageEtudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stage")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_etudiant", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_partenaire", nullable = false)
    private Partenaire partenaire;

    private String sujet;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutStage statut = StatutStage.EN_ATTENTE;

    @Column(name = "tuteur_entreprise")
    private String tuteurEntreprise;

    @Column(name = "tuteur_universite")
    private String tuteurUniversite;

    @Builder.Default
    @Column(name = "convention_signee")
    private Boolean conventionSignee = false;

    @Column(columnDefinition = "TEXT")
    private String commentaire;
}
