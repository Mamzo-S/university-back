package com.universite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promotion")
    private Long id;

    private String nom;

    @Column
    private String titre;

    @Column(unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String anneeAcademique;

    @ManyToOne
    @JoinColumn(name = "id_annee_academique")
    private AnneeAcademique anneeAcademiqueRef;

    @ManyToOne
    @JoinColumn(name = "formation_id")
    private Formation formation;

    @OneToMany(mappedBy = "promotion")
    @JsonIgnore
    @Builder.Default
    private List<Etudiant> etudiants = new ArrayList<>();

    @OneToOne(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private EmploiDuTemps emploiDuTemps;
}
