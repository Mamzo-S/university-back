package com.universite.entity;

/**
 * Jour de la semaine pour une séance planifiée.
 * 0 = lundi … 6 = dimanche (aligné sur le front {@code dayOfWeek}).
 */
public enum JourSemaine {
    LUNDI(0),
    MARDI(1),
    MERCREDI(2),
    JEUDI(3),
    VENDREDI(4),
    SAMEDI(5),
    DIMANCHE(6);

    private final int index;

    JourSemaine(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static JourSemaine fromIndex(int index) {
        for (JourSemaine jour : values()) {
            if (jour.index == index) {
                return jour;
            }
        }
        throw new IllegalArgumentException("Jour de semaine invalide: " + index);
    }
}
