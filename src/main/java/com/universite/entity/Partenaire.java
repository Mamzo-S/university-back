package com.universite.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "partenaires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partenaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String secteur;

    private String email;

    private String telephone;

    private String adresse;

    private String ville;

    private String pays;

    @Column(name = "contact_nom")
    private String contactNom;

    @Column(name = "contact_fonction")
    private String contactFonction;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    private Boolean actif = true;

    @Builder.Default
    @Column(name = "convention_cadre")
    private Boolean conventionCadre = false;
}
