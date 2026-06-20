package com.universite.mapper;

import com.universite.dto.PromotionResponse;
import com.universite.entity.Promotion;

public final class PromotionMapper {

    private PromotionMapper() {
    }

    public static PromotionResponse toResponse(Promotion promotion) {
        String titre = resolveTitre(promotion);
        return PromotionResponse.builder()
                .id(promotion.getId())
                .titre(titre)
                .nom(titre)
                .slug(promotion.getSlug())
                .description(promotion.getDescription())
                .anneeAcademique(resolveAnneeLabel(promotion))
                .anneeAcademiqueId(
                        promotion.getAnneeAcademiqueRef() != null
                                ? promotion.getAnneeAcademiqueRef().getId()
                                : null
                )
                .anneeAcademiqueTitre(
                        promotion.getAnneeAcademiqueRef() != null
                                ? promotion.getAnneeAcademiqueRef().getTitre()
                                : promotion.getAnneeAcademique()
                )
                .formationId(
                        promotion.getFormation() != null
                                ? promotion.getFormation().getId()
                                : null
                )
                .formationNom(
                        promotion.getFormation() != null
                                ? FormationMapper.resolveTitre(promotion.getFormation())
                                : null
                )
                .effectif(promotion.getEtudiants() != null ? promotion.getEtudiants().size() : 0)
                .build();
    }

    public static String resolveTitre(Promotion promotion) {
        if (promotion.getTitre() != null && !promotion.getTitre().isBlank()) {
            return promotion.getTitre();
        }
        return promotion.getNom();
    }

    private static String resolveAnneeLabel(Promotion promotion) {
        if (promotion.getAnneeAcademiqueRef() != null) {
            return promotion.getAnneeAcademiqueRef().getTitre();
        }
        return promotion.getAnneeAcademique();
    }
}
