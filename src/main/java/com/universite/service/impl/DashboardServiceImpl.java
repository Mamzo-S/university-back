package com.universite.service.impl;

import com.universite.dto.StatistiqueDTO;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.FormationRepository;
import com.universite.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl
        implements DashboardService {

    private final EtudiantRepository etudiantRepository;
    private final FormationRepository formationRepository;

    @Override
    public StatistiqueDTO getStatistiques() {

        long totalEtudiants =
                etudiantRepository.count();

        long totalFormations =
                formationRepository.count();

        long totalHommes =
                etudiantRepository
                        .countByGenreIgnoreCase("Homme");

        long totalFemmes =
                etudiantRepository
                        .countByGenreIgnoreCase("Femme");

        return StatistiqueDTO.builder()
                .totalEtudiants(totalEtudiants)
                .totalFormations(totalFormations)
                .totalHommes(totalHommes)
                .totalFemmes(totalFemmes)
                .build();
    }
}