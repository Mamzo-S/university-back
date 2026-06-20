package com.universite.mapper;

import com.universite.dto.FormationResponse;
import com.universite.entity.Formation;

public final class FormationMapper {

    private FormationMapper() {
    }

    public static FormationResponse toResponse(Formation formation) {
        String titre = resolveTitre(formation);
        return FormationResponse.builder()
                .id(formation.getId())
                .titre(titre)
                .nom(titre)
                .slug(formation.getSlug())
                .description(formation.getDescription())
                .imageUrl(formation.getImageUrl())
                .niveau(
                        formation.getNiveau() != null
                                ? formation.getNiveau().name()
                                : null
                )
                .typeFormation(formation.getTypeFormation())
                .typeFinancement(formation.getTypeFinancement())
                .dateDebut(
                        formation.getDateDebut() != null
                                ? formation.getDateDebut().toString()
                                : null
                )
                .dateFin(
                        formation.getDateFin() != null
                                ? formation.getDateFin().toString()
                                : null
                )
                .montant(formation.getMontant())
                .filiereId(
                        formation.getFiliere() != null
                                ? formation.getFiliere().getId()
                                : null
                )
                .filiereNom(
                        formation.getFiliere() != null
                                ? formation.getFiliere().getNom()
                                : null
                )
                .build();
    }

    public static String resolveTitre(Formation formation) {
        if (formation.getTitre() != null && !formation.getTitre().isBlank()) {
            return formation.getTitre();
        }
        return formation.getNom();
    }
}
