package com.universite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "formations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Column
    private String titre;

    @Column(unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    private String typeFormation;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau")
    private NiveauEtude niveau;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private BigDecimal montant;

    private String typeFinancement;

    // =========================
    // RELATION FILIERE
    // =========================

    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    // =========================
    // RELATION PROMOTIONS
    // =========================

    @OneToMany(mappedBy = "formation")
    @JsonIgnore
    private List<Promotion> promotions;
}

