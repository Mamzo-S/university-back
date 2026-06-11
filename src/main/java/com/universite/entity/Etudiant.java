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
    private Long id;

    @NotBlank(message = "INE obligatoire")
    @Column(unique = true)
    private String ine;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotNull(message = "La date de naissance est obligatoire")
    private LocalDate dateNaissance;

    @NotBlank(message = "Le genre est obligatoire")
    private String genre;

    @NotNull(message = "Année début obligatoire")
    private Integer anneeDebut;

    @NotNull(message = "Année sortie obligatoire")
    private Integer anneeSortie;

    @ManyToOne
    @JoinColumn(name = "formation_id")
    private Formation formation;
}