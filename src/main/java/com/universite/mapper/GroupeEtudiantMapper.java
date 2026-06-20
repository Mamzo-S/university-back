package com.universite.mapper;

import com.universite.dto.GroupeEtudiantResponse;
import com.universite.entity.Formation;
import com.universite.entity.GroupeEtudiant;
import com.universite.entity.Promotion;

public final class GroupeEtudiantMapper {

    private GroupeEtudiantMapper() {
    }

    public static GroupeEtudiantResponse toResponse(GroupeEtudiant groupe) {
        Promotion promotion = groupe.getPromotion();
        Formation formation = promotion != null ? promotion.getFormation() : null;

        return GroupeEtudiantResponse.builder()
                .id(groupe.getId())
                .titre(groupe.getTitre())
                .slug(groupe.getSlug())
                .description(groupe.getDescription())
                .promotionId(promotion != null ? promotion.getId() : null)
                .promotionTitre(promotion != null ? PromotionMapper.resolveTitre(promotion) : null)
                .formationId(formation != null ? formation.getId() : null)
                .formationNom(formation != null ? FormationMapper.resolveTitre(formation) : null)
                .filiereId(
                        formation != null && formation.getFiliere() != null
                                ? formation.getFiliere().getId()
                                : null
                )
                .filiereNom(
                        formation != null && formation.getFiliere() != null
                                ? formation.getFiliere().getNom()
                                : null
                )
                .effectif(groupe.getEtudiants() != null ? groupe.getEtudiants().size() : 0)
                .build();
    }
}
