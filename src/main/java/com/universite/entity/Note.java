package com.universite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @Column(nullable = false)
    private String typeEvaluation; // DEVOIR, EXAMEN, PROJET, TD

    @Column(nullable = false)
    private Double valeur;

    @Column(nullable = false)
    private String anneeAcademique;

    @Column(nullable = false)
    private String semestre;

    @Column(nullable = false)
    private Boolean bulletinPublie = false;

    private LocalDate datePublication;
}
