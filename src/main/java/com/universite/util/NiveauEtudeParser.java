package com.universite.util;

import com.universite.entity.NiveauEtude;

public final class NiveauEtudeParser {

    private NiveauEtudeParser() {
    }

    public static NiveauEtude parseRequired(String value) {
        NiveauEtude niveau = parse(value);
        if (niveau == null) {
            throw new RuntimeException("Le niveau est obligatoire");
        }
        return niveau;
    }

    public static NiveauEtude parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase().replace(' ', '_');
        try {
            return NiveauEtude.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return mapLegacyLabel(value.trim());
        }
    }

    private static NiveauEtude mapLegacyLabel(String value) {
        String lower = value.toLowerCase();
        if (lower.contains("doctorat") || lower.contains("doctora")) {
            return NiveauEtude.DOCTORAT;
        }
        if (lower.contains("master") && lower.contains("2")) {
            return NiveauEtude.MASTER_2;
        }
        if (lower.contains("master") && lower.contains("1")) {
            return NiveauEtude.MASTER_1;
        }
        if (lower.contains("master")) {
            return NiveauEtude.MASTER_1;
        }
        if (lower.contains("licence") && lower.contains("3")) {
            return NiveauEtude.LICENCE_3;
        }
        if (lower.contains("licence") && lower.contains("2")) {
            return NiveauEtude.LICENCE_2;
        }
        if (lower.contains("licence") && lower.contains("1")) {
            return NiveauEtude.LICENCE_1;
        }
        if (lower.matches("l3")) {
            return NiveauEtude.LICENCE_3;
        }
        if (lower.matches("l2")) {
            return NiveauEtude.LICENCE_2;
        }
        if (lower.matches("l1")) {
            return NiveauEtude.LICENCE_1;
        }
        if (lower.matches("m2")) {
            return NiveauEtude.MASTER_2;
        }
        if (lower.matches("m1")) {
            return NiveauEtude.MASTER_1;
        }

        throw new RuntimeException(
                "Niveau invalide : « " + value + " ». Valeurs attendues : "
                        + "LICENCE_1, LICENCE_2, LICENCE_3, MASTER_1, MASTER_2, DOCTORAT"
        );
    }
}
