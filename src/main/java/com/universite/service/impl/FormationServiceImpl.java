package com.universite.service.impl;

import com.universite.entity.Formation;
import com.universite.repository.FormationRepository;
import com.universite.service.FormationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FormationServiceImpl implements FormationService {

    private final FormationRepository formationRepository;

    @Override
    public Formation ajouterFormation(Formation formation) {
        return formationRepository.save(formation);
    }

    @Override
    public List<Formation> getAllFormations() {
        return formationRepository.findAll();
    }
}

