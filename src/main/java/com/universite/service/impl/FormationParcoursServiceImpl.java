package com.universite.service.impl;

import com.universite.dto.parcours.FormationParcoursDto;
import com.universite.entity.Formation;
import com.universite.mapper.FormationParcoursMapper;
import com.universite.repository.FormationRepository;
import com.universite.service.FormationAccessService;
import com.universite.service.FormationParcoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormationParcoursServiceImpl implements FormationParcoursService {

    private final FormationRepository formationRepository;
    private final FormationParcoursMapper formationParcoursMapper;
    private final FormationAccessService formationAccessService;

    @Override
    @Transactional(readOnly = true)
    public FormationParcoursDto getParcours(Long formationId, String userEmail) {
        Formation formation = findFormation(formationId);
        formationAccessService.assertCanReadFormation(formation, userEmail);
        return formationParcoursMapper.toDto(formation);
    }

    @Override
    @Transactional(readOnly = true)
    public FormationParcoursDto getParcoursBySlug(String slug, String userEmail) {
        Formation formation = findFormationBySlugWithFiliere(slug);
        formationAccessService.assertCanReadFormation(formation, userEmail);
        return formationParcoursMapper.toDto(formation);
    }

    @Override
    @Transactional
    public FormationParcoursDto updateParcours(
            Long formationId,
            FormationParcoursDto parcours,
            String userEmail
    ) {
        Formation formation = findFormation(formationId);
        formationAccessService.assertCanUpdateParcours(formation, userEmail);
        formationParcoursMapper.applyToFormation(formation, parcours);
        return formationParcoursMapper.toDto(formationRepository.save(formation));
    }

    private Formation findFormation(Long formationId) {
        return formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
    }

    private Formation findFormationBySlugWithFiliere(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new RuntimeException("Slug de formation invalide");
        }
        return formationRepository.findBySlugWithFiliere(slug.trim())
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
    }
}
