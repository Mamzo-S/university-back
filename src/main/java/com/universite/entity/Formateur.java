package com.universite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "formateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Formateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_formateur")
    private Long id;

    private String grade;

    private String specialite;

    @OneToOne
    @JoinColumn(name = "id_utilisateur", unique = true, nullable = false)
    private Utilisateur utilisateur;

    @ManyToMany
    @JoinTable(
            name = "formateur_formations",
            joinColumns = @JoinColumn(name = "id_formateur"),
            inverseJoinColumns = @JoinColumn(name = "formation_id")
    )
    @JsonIgnore
    @Builder.Default
    private Set<Formation> formations = new HashSet<>();
}
