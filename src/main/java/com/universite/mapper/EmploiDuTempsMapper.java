package com.universite.mapper;

import com.universite.dto.EmploiDuTempsResponse;
import com.universite.dto.SeanceResponse;
import com.universite.entity.EmploiDuTemps;
import com.universite.entity.Seance;

import java.util.Comparator;
import java.util.List;

public final class EmploiDuTempsMapper {

    private EmploiDuTempsMapper() {
    }

    public static EmploiDuTempsResponse toResponse(EmploiDuTemps emploiDuTemps) {
        var promotion = emploiDuTemps.getPromotion();
        var formation = promotion.getFormation();

        List<SeanceResponse> seances = emploiDuTemps.getSeances().stream()
                .sorted(Comparator
                        .comparing((Seance s) -> s.getJourSemaine().getIndex())
                        .thenComparing(Seance::getHeureDebut))
                .map(SeanceMapper::toResponse)
                .toList();

        return EmploiDuTempsResponse.builder()
                .id(emploiDuTemps.getId())
                .promotionId(promotion.getId())
                .promotionNom(PromotionMapper.resolveTitre(promotion))
                .anneeAcademique(promotion.getAnneeAcademique())
                .formationId(formation != null ? formation.getId() : null)
                .formationNom(formation != null ? FormationMapper.resolveTitre(formation) : null)
                .libelle(emploiDuTemps.getLibelle())
                .publie(Boolean.TRUE.equals(emploiDuTemps.getPublie()))
                .effectif(promotion.getEtudiants() != null ? promotion.getEtudiants().size() : 0)
                .seances(seances)
                .build();
    }
}
