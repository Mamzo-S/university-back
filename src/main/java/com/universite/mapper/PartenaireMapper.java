package com.universite.mapper;

import com.universite.dto.PartenaireResponse;
import com.universite.entity.Partenaire;

public final class PartenaireMapper {

    private PartenaireMapper() {
    }

    public static PartenaireResponse toResponse(Partenaire partenaire, long stageCount) {
        return PartenaireResponse.builder()
                .id(partenaire.getId())
                .nom(partenaire.getNom())
                .secteur(partenaire.getSecteur())
                .email(partenaire.getEmail())
                .telephone(partenaire.getTelephone())
                .adresse(partenaire.getAdresse())
                .ville(partenaire.getVille())
                .pays(partenaire.getPays())
                .contactNom(partenaire.getContactNom())
                .contactFonction(partenaire.getContactFonction())
                .description(partenaire.getDescription())
                .actif(partenaire.getActif())
                .conventionCadre(partenaire.getConventionCadre())
                .stageCount(stageCount)
                .build();
    }
}
