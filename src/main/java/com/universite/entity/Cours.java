package com.universite.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String semestre;

    @Column(nullable = false)
    private Double coefficient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "formation_id", nullable = false)
    private Formation formation;
}
