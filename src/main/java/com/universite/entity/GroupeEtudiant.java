package com.universite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groupes_etudiants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupeEtudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_groupe_etudiant")
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_promotion", nullable = false)
    private Promotion promotion;

    @OneToMany(mappedBy = "groupeEtudiant")
    @JsonIgnore
    @Builder.Default
    private List<Etudiant> etudiants = new ArrayList<>();
}
