package com.universite.mapper;

import com.universite.dto.FiliereDetailResponse;
import com.universite.dto.FiliereEtudiantSummary;
import com.universite.dto.FiliereModuleSummary;
import com.universite.dto.FiliereResponse;
import com.universite.entity.Etudiant;
import com.universite.entity.Filiere;
import com.universite.entity.Formation;
import com.universite.entity.Utilisateur;

import java.util.Comparator;
import java.util.List;

public final class FiliereMapper {

    private FiliereMapper() {
    }

    public static FiliereResponse toResponse(Filiere filiere, long moduleCount, long etudiantCount) {
        return FiliereResponse.builder()
                .id(filiere.getId())
                .nom(filiere.getNom())
                .description(filiere.getDescription())
                .moduleCount(moduleCount)
                .etudiantCount(etudiantCount)
                .build();
    }

    public static FiliereDetailResponse toDetailResponse(
            Filiere filiere,
            List<Formation> modules,
            List<Etudiant> etudiants,
            FormationParcoursMapper parcoursMapper
    ) {
        return FiliereDetailResponse.builder()
                .id(filiere.getId())
                .nom(filiere.getNom())
                .description(filiere.getDescription())
                .moduleCount(modules.size())
                .etudiantCount(etudiants.size())
                .modules(modules.stream()
                        .sorted(Comparator.comparing(
                                FormationMapper::resolveTitre,
                                Comparator.nullsLast(String::compareToIgnoreCase)
                        ))
                        .map(formation -> toModuleSummary(
                                formation,
                                parcoursMapper.countSubModules(formation)
                        ))
                        .toList())
                .etudiants(etudiants.stream()
                        .sorted(Comparator.comparing(
                                FiliereMapper::resolveEtudiantName,
                                Comparator.nullsLast(String::compareToIgnoreCase)
                        ))
                        .map(FiliereMapper::toEtudiantSummary)
                        .toList())
                .build();
    }

    private static FiliereModuleSummary toModuleSummary(Formation formation, int subModuleCount) {
        return FiliereModuleSummary.builder()
                .id(formation.getId())
                .titre(FormationMapper.resolveTitre(formation))
                .slug(formation.getSlug())
                .description(formation.getDescription())
                .imageUrl(formation.getImageUrl())
                .niveau(
                        formation.getNiveau() != null
                                ? formation.getNiveau().name()
                                : null
                )
                .typeFormation(formation.getTypeFormation())
                .subModuleCount(subModuleCount)
                .build();
    }

    private static FiliereEtudiantSummary toEtudiantSummary(Etudiant etudiant) {
        Utilisateur utilisateur = etudiant.getUtilisateur();
        return FiliereEtudiantSummary.builder()
                .id(etudiant.getId())
                .prenom(utilisateur != null ? utilisateur.getPrenom() : null)
                .nom(utilisateur != null ? utilisateur.getNom() : null)
                .email(utilisateur != null ? utilisateur.getEmail() : null)
                .ine(etudiant.getIne())
                .promotionNom(
                        etudiant.getPromotion() != null
                                ? PromotionMapper.resolveTitre(etudiant.getPromotion())
                                : null
                )
                .formationNom(
                        etudiant.getPromotion() != null && etudiant.getPromotion().getFormation() != null
                                ? FormationMapper.resolveTitre(etudiant.getPromotion().getFormation())
                                : null
                )
                .build();
    }

    private static String resolveEtudiantName(Etudiant etudiant) {
        Utilisateur utilisateur = etudiant.getUtilisateur();
        if (utilisateur == null) {
            return "";
        }
        return ((utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "") + " "
                + (utilisateur.getNom() != null ? utilisateur.getNom() : "")).trim();
    }
}
