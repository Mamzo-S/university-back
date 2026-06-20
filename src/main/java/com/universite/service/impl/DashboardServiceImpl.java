package com.universite.service.impl;

import com.universite.dto.StatistiqueDTO;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.FormateurRepository;
import com.universite.repository.FormationRepository;
import com.universite.repository.PersonnelRepository;
import com.universite.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EtudiantRepository etudiantRepository;
    private final FormationRepository formationRepository;
    private final FormateurRepository formateurRepository;
    private final PersonnelRepository personnelRepository;

    @Override
    public StatistiqueDTO getStatistiques() {
        return StatistiqueDTO.builder()
                .totalEtudiants(etudiantRepository.count())
                .totalFormations(formationRepository.count())
                .totalHommes(0)
                .totalFemmes(0)
                .totalFormateurs(formateurRepository.count())
                .totalPersonnels(personnelRepository.count())
                .build();
    }
}
