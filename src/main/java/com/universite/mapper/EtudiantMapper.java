package com.universite.mapper;

import com.universite.dto.EtudiantDTO;
import com.universite.entity.Etudiant;
import com.universite.entity.Utilisateur;

public class EtudiantMapper {

    public static EtudiantDTO toDTO(Etudiant etudiant) {
        Utilisateur utilisateur = etudiant.getUtilisateur();

        return EtudiantDTO.builder()
                .id(etudiant.getId())
                .ine(etudiant.getIne())
                .utilisateurId(utilisateur != null ? utilisateur.getId() : null)
                .nom(utilisateur != null ? utilisateur.getNom() : null)
                .prenom(utilisateur != null ? utilisateur.getPrenom() : null)
                .email(utilisateur != null ? utilisateur.getEmail() : null)
                .dateNaissance(etudiant.getDateNaissance())
                .niveau(etudiant.getNiveau())
                .promotionId(
                        etudiant.getPromotion() != null
                                ? etudiant.getPromotion().getId()
                                : null
                )
                .promotionNom(
                        etudiant.getPromotion() != null
                                ? com.universite.mapper.PromotionMapper.resolveTitre(etudiant.getPromotion())
                                : null
                )
                .formationId(
                        etudiant.getPromotion() != null
                                && etudiant.getPromotion().getFormation() != null
                                ? etudiant.getPromotion().getFormation().getId()
                                : null
                )
                .formationNom(
                        etudiant.getPromotion() != null
                                && etudiant.getPromotion().getFormation() != null
                                ? com.universite.mapper.FormationMapper.resolveTitre(
                                        etudiant.getPromotion().getFormation())
                                : null
                )
                .filiereId(
                        etudiant.getFiliere() != null
                                ? etudiant.getFiliere().getId()
                                : null
                )
                .filiereNom(
                        etudiant.getFiliere() != null
                                ? etudiant.getFiliere().getNom()
                                : null
                )
                .groupeEtudiantId(
                        etudiant.getGroupeEtudiant() != null
                                ? etudiant.getGroupeEtudiant().getId()
                                : null
                )
                .groupeEtudiantNom(
                        etudiant.getGroupeEtudiant() != null
                                ? etudiant.getGroupeEtudiant().getTitre()
                                : null
                )
                .build();
    }
}
