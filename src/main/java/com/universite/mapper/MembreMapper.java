package com.universite.mapper;

import com.universite.dto.EtudiantDTO;
import com.universite.dto.MembreResponse;
import com.universite.entity.*;
import com.universite.entity.RoleName;

public final class MembreMapper {

    private MembreMapper() {
    }

    public static MembreResponse fromUtilisateur(Utilisateur utilisateur, RoleName role) {
        return MembreResponse.builder()
                .id(utilisateur.getId())
                .utilisateurId(utilisateur.getId())
                .prenom(utilisateur.getPrenom())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .actif(Boolean.TRUE.equals(utilisateur.getActif()))
                .role(role.name())
                .build();
    }

    public static MembreResponse fromPersonnel(Personnel personnel) {
        Utilisateur utilisateur = personnel.getUtilisateur();
        RoleName role = mapTypeToRole(personnel.getType());

        return MembreResponse.builder()
                .id(personnel.getId())
                .utilisateurId(utilisateur.getId())
                .prenom(utilisateur.getPrenom())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .actif(Boolean.TRUE.equals(utilisateur.getActif()))
                .role(role.name())
                .fonction(personnel.getFonction())
                .service(personnel.getService())
                .build();
    }

    public static MembreResponse fromFormateur(Formateur formateur) {
        Utilisateur utilisateur = formateur.getUtilisateur();
        var formations = formateur.getFormations() != null ? formateur.getFormations() : java.util.List.<Formation>of();

        return MembreResponse.builder()
                .id(formateur.getId())
                .utilisateurId(utilisateur.getId())
                .prenom(utilisateur.getPrenom())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .actif(Boolean.TRUE.equals(utilisateur.getActif()))
                .role(RoleName.FORMATEUR.name())
                .grade(formateur.getGrade())
                .specialite(formateur.getSpecialite())
                .service(formateur.getGrade())
                .formationIds(formations.stream().map(Formation::getId).toList())
                .formationNoms(formations.stream().map(FormationMapper::resolveTitre).toList())
                .build();
    }

    public static MembreResponse fromEtudiant(EtudiantDTO etudiant) {
        return MembreResponse.builder()
                .id(etudiant.getId())
                .utilisateurId(etudiant.getUtilisateurId())
                .prenom(etudiant.getPrenom())
                .nom(etudiant.getNom())
                .email(etudiant.getEmail())
                .actif(true)
                .role(RoleName.ETUDIANT.name())
                .ine(etudiant.getIne())
                .dateNaissance(
                        etudiant.getDateNaissance() != null
                                ? etudiant.getDateNaissance().toString()
                                : null
                )
                .niveau(
                        etudiant.getNiveau() != null
                                ? etudiant.getNiveau().name()
                                : null
                )
                .promotionNom(etudiant.getPromotionNom())
                .formationNom(etudiant.getFormationNom())
                .filiereId(etudiant.getFiliereId())
                .filiereNom(etudiant.getFiliereNom())
                .promotionId(etudiant.getPromotionId())
                .groupeEtudiantId(etudiant.getGroupeEtudiantId())
                .groupeEtudiantNom(etudiant.getGroupeEtudiantNom())
                .build();
    }

    private static RoleName mapTypeToRole(TypePersonnel type) {
        return switch (type) {
            case PERSONNEL_ADMIN -> RoleName.PERSONNEL_ADMIN;
            case TUTEUR -> RoleName.TUTEUR;
            case RESPONSABLE_FORMATION -> RoleName.RESPONSABLE_FORMATION;
            case SERVICE_INSERTION -> RoleName.SERVICE_INSERTION;
        };
    }
}
