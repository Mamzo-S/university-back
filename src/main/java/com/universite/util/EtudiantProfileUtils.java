package com.universite.util;

import com.universite.entity.Etudiant;
import com.universite.entity.Filiere;
import com.universite.entity.Formation;
import com.universite.entity.NiveauEtude;
import com.universite.entity.Promotion;
import com.universite.repository.FormationRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EtudiantProfileUtils {

    private EtudiantProfileUtils() {
    }

    /**
     * Filière académique de l'étudiant (profil), sans inférence via la promotion.
     */
    public static Filiere getStudentFiliere(Etudiant etudiant) {
        return etudiant.getFiliere();
    }

    /**
     * @deprecated Préférer {@link #getStudentFiliere(Etudiant)} pour le périmètre pédagogique.
     */
    @Deprecated
    public static Filiere resolveFiliere(Etudiant etudiant) {
        Filiere filiere = etudiant.getFiliere();
        if (filiere == null && etudiant.getPromotion() != null
                && etudiant.getPromotion().getFormation() != null
                && etudiant.getPromotion().getFormation().getFiliere() != null) {
            filiere = etudiant.getPromotion().getFormation().getFiliere();
        }
        return filiere;
    }

    public static List<Formation> findAccessibleModules(
            Etudiant etudiant,
            FormationRepository formationRepository
    ) {
        Filiere filiere = getStudentFiliere(etudiant);
        if (filiere == null) {
            return List.of();
        }

        NiveauEtude niveauEtudiant = etudiant.getNiveau();
        List<Formation> modulesByScope = niveauEtudiant != null
                ? formationRepository.findByFiliereIdAndNiveau(filiere.getId(), niveauEtudiant)
                : formationRepository.findByFiliereId(filiere.getId());

        return mergeAccessibleModules(etudiant, modulesByScope);
    }

    /**
     * Modules accessibles : filière + niveau de l'étudiant, plus éventuellement le module
     * de sa promotion s'il appartient à la même filière.
     */
    public static List<Formation> mergeAccessibleModules(
            Etudiant etudiant,
            List<Formation> modulesByFiliereAndNiveau
    ) {
        Filiere studentFiliere = getStudentFiliere(etudiant);
        if (studentFiliere == null) {
            return List.of();
        }

        Set<Long> seen = new LinkedHashSet<>();
        List<Formation> modules = new ArrayList<>();

        for (Formation formation : modulesByFiliereAndNiveau) {
            if (belongsToFiliere(formation, studentFiliere) && seen.add(formation.getId())) {
                modules.add(formation);
            }
        }

        Promotion promotion = etudiant.getPromotion();
        if (promotion != null && promotion.getFormation() != null) {
            Formation promotionModule = promotion.getFormation();
            if (belongsToFiliere(promotionModule, studentFiliere) && seen.add(promotionModule.getId())) {
                modules.add(promotionModule);
            }
        }

        return modules;
    }

    public static boolean belongsToFiliere(Formation formation, Filiere filiere) {
        if (formation == null || filiere == null || formation.getFiliere() == null) {
            return false;
        }
        return filiere.getId().equals(formation.getFiliere().getId());
    }
}
