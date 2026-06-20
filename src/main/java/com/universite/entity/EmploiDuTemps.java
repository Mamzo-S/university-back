package com.universite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Emploi du temps d'une promotion (classe).
 * Une promotion possède exactement un emploi du temps ; les créneaux sont des {@link Seance}.
 */
@Entity
@Table(name = "emplois_du_temps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploiDuTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_emploi_du_temps")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_promotion", unique = true, nullable = false)
    private Promotion promotion;

    /** Libellé affiché (ex. « EDT L3 Marketing 2025-2026 »). */
    private String libelle;

    @Builder.Default
    @Column(nullable = false)
    private Boolean publie = false;

    @OneToMany(mappedBy = "emploiDuTemps", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Seance> seances = new ArrayList<>();

    public void addSeance(Seance seance) {
        seances.add(seance);
        seance.setEmploiDuTemps(this);
    }
}
