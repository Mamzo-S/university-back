package com.universite.mapper;

import com.universite.dto.StageResponse;
import com.universite.entity.Etudiant;
import com.universite.entity.StageEtudiant;
import com.universite.entity.Utilisateur;

public final class StageMapper {

    private StageMapper() {
    }

    public static StageResponse toResponse(StageEtudiant stage) {
        Etudiant etudiant = stage.getEtudiant();
        Utilisateur utilisateur = etudiant != null ? etudiant.getUtilisateur() : null;

        return StageResponse.builder()
                .id(stage.getId())
                .etudiantId(etudiant != null ? etudiant.getId() : null)
                .etudiantNom(utilisateur != null ? utilisateur.getNom() : null)
                .etudiantPrenom(utilisateur != null ? utilisateur.getPrenom() : null)
                .etudiantEmail(utilisateur != null ? utilisateur.getEmail() : null)
                .etudiantIne(etudiant != null ? etudiant.getIne() : null)
                .filiereNom(
                        etudiant != null && etudiant.getFiliere() != null
                                ? etudiant.getFiliere().getNom()
                                : null
                )
                .partenaireId(stage.getPartenaire() != null ? stage.getPartenaire().getId() : null)
                .partenaireNom(stage.getPartenaire() != null ? stage.getPartenaire().getNom() : null)
                .sujet(stage.getSujet())
                .description(stage.getDescription())
                .dateDebut(stage.getDateDebut() != null ? stage.getDateDebut().toString() : null)
                .dateFin(stage.getDateFin() != null ? stage.getDateFin().toString() : null)
                .statut(stage.getStatut() != null ? stage.getStatut().name() : null)
                .tuteurEntreprise(stage.getTuteurEntreprise())
                .tuteurUniversite(stage.getTuteurUniversite())
                .conventionSignee(stage.getConventionSignee())
                .commentaire(stage.getCommentaire())
                .build();
    }
}
