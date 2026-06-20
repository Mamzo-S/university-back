package com.universite.util;

import com.universite.entity.Etudiant;
import com.universite.entity.Filiere;
import com.universite.entity.Formation;
import com.universite.entity.Promotion;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EtudiantProfileUtils {

    private EtudiantProfileUtils() {
    }

    public static Filiere resolveFiliere(Etudiant etudiant) {
        Filiere filiere = etudiant.getFiliere();
        if (filiere == null && etudiant.getPromotion() != null
                && etudiant.getPromotion().getFormation() != null
                && etudiant.getPromotion().getFormation().getFiliere() != null) {
            filiere = etudiant.getPromotion().getFormation().getFiliere();
        }
        return filiere;
    }

    /**
     * Modules accessibles : filière + niveau, plus le module rattaché à la promotion.
     */
    public static List<Formation> mergeAccessibleModules(
            Etudiant etudiant,
            List<Formation> modulesByFiliereAndNiveau
    ) {
        Set<Long> seen = new LinkedHashSet<>();
        List<Formation> modules = new ArrayList<>();

        for (Formation formation : modulesByFiliereAndNiveau) {
            if (seen.add(formation.getId())) {
                modules.add(formation);
            }
        }

        Promotion promotion = etudiant.getPromotion();
        if (promotion != null && promotion.getFormation() != null) {
            Formation promotionModule = promotion.getFormation();
            if (seen.add(promotionModule.getId())) {
                modules.add(promotionModule);
            }
        }

        return modules;
    }
}
