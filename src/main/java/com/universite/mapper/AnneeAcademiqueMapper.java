package com.universite.mapper;

import com.universite.dto.AnneeAcademiqueResponse;
import com.universite.entity.AnneeAcademique;

public final class AnneeAcademiqueMapper {

    private AnneeAcademiqueMapper() {
    }

    public static AnneeAcademiqueResponse toResponse(AnneeAcademique annee) {
        return AnneeAcademiqueResponse.builder()
                .id(annee.getId())
                .titre(annee.getTitre())
                .slug(annee.getSlug())
                .description(annee.getDescription())
                .build();
    }
}
