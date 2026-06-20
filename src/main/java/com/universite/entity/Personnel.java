package com.universite.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personnels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Personnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_personnel")
    private Long id;

    private String fonction;

    private String service;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_personnel", nullable = false)
    private TypePersonnel type;

    @OneToOne
    @JoinColumn(name = "id_utilisateur", unique = true, nullable = false)
    private Utilisateur utilisateur;
}
