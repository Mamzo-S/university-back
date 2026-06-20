package com.universite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Créneau planifié dans un emploi du temps :
 * module ({@link Cours}) + enseignant ({@link Formateur}) + horaire + salle.
 */
@Entity
@Table(name = "seances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seance")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_emploi_du_temps", nullable = false)
    private EmploiDuTemps emploiDuTemps;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cours", nullable = false)
    private Cours cours;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_formateur", nullable = false)
    private Formateur formateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "jour_semaine", nullable = false)
    private JourSemaine jourSemaine;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    private String salle;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_seance", nullable = false)
    private TypeSeance typeSeance;
}
