package com.universite.mapper;

import com.universite.dto.SeanceResponse;
import com.universite.entity.*;
import com.universite.mapper.FormationMapper;
import com.universite.mapper.PromotionMapper;

public final class SeanceMapper {

    private SeanceMapper() {
    }

    public static SeanceResponse toResponse(Seance seance) {
        Cours cours = seance.getCours();
        Formateur formateur = seance.getFormateur();
        EmploiDuTemps emploiDuTemps = seance.getEmploiDuTemps();
        Promotion promotion = emploiDuTemps.getPromotion();
        Utilisateur enseignant = formateur.getUtilisateur();
        Formation formation = cours.getFormation() != null
                ? cours.getFormation()
                : promotion.getFormation();
        String formationLabel = formation != null ? FormationMapper.resolveTitre(formation) : null;

        return SeanceResponse.builder()
                .id(seance.getId())
                .emploiDuTempsId(emploiDuTemps.getId())
                .coursId(cours.getId())
                .coursCode(cours.getCode())
                .coursNom(formationLabel != null ? formationLabel : cours.getNom())
                .formateurId(formateur.getId())
                .formateurNom(formatNom(enseignant))
                .promotionId(promotion.getId())
                .promotionNom(PromotionMapper.resolveTitre(promotion))
                .formationId(formation != null ? formation.getId() : null)
                .formationNom(formationLabel)
                .jourSemaine(seance.getJourSemaine().getIndex())
                .heureDebut(seance.getHeureDebut().toString())
                .heureFin(seance.getHeureFin().toString())
                .salle(seance.getSalle())
                .typeSeance(seance.getTypeSeance().name())
                .build();
    }

    private static String formatNom(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return "";
        }
        String prenom = utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "";
        String nom = utilisateur.getNom() != null ? utilisateur.getNom() : "";
        return (prenom + " " + nom).trim();
    }
}
