package com.universite.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "annees_academiques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnneeAcademique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_annee_academique")
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;
}
