package com.universite.util;

import com.universite.entity.Etudiant;
import com.universite.entity.Filiere;

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
}
