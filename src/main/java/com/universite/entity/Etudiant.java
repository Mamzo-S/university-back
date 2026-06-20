package com.universite.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "etudiants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etudiant")
    private Long id;

    @NotBlank(message = "INE obligatoire")
    @Column(unique = true)
    private String ine;

    @NotNull(message = "La date de naissance est obligatoire")
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "annee_entree")
    private Integer anneeEntree;

    @Column(name = "annee_sortie")
    private Integer anneeSortie;

    @NotNull(message = "Le niveau est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "niveau", nullable = false)
    private NiveauEtude niveau;

    @OneToOne
    @JoinColumn(name = "id_utilisateur", unique = true, nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "id_filiere")
    private Filiere filiere;

    @ManyToOne
    @JoinColumn(name = "id_promotion")
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "id_groupe_etudiant")
    private GroupeEtudiant groupeEtudiant;
}
