package com.universite.mapper;

import com.universite.dto.EtudiantDTO;
import com.universite.entity.Etudiant;

public class EtudiantMapper {

    public static EtudiantDTO toDTO(Etudiant etudiant) {

        return EtudiantDTO.builder()
                .id(etudiant.getId())
                .ine(etudiant.getIne())
                .nom(etudiant.getNom())
                .prenom(etudiant.getPrenom())
                .dateNaissance(etudiant.getDateNaissance())
                .genre(etudiant.getGenre())
                .anneeDebut(etudiant.getAnneeDebut())
                .anneeSortie(etudiant.getAnneeSortie())
                .formationId(
                        etudiant.getFormation() != null
                                ? etudiant.getFormation().getId()
                                : null
                )
                .formationNom(
                        etudiant.getFormation() != null
                                ? etudiant.getFormation().getNom()
                                : null
                )
                .build();
    }
}
