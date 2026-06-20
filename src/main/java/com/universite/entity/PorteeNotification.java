package com.universite.entity;

/**
 * Périmètre de diffusion pour les notifications étudiants.
 */
public enum PorteeNotification {
    /** Tous les étudiants de la filière, quel que soit le niveau. */
    FILIERE,
    /** Tous les étudiants du niveau, quelle que soit la filière. */
    NIVEAU,
    /** Intersection filière + niveau. */
    FILIERE_ET_NIVEAU
}
