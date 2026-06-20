package com.universite.service.impl;

import com.universite.dto.FormateurFormationSummary;
import com.universite.dto.MembreResponse;
import com.universite.entity.Formateur;
import com.universite.entity.Formation;
import com.universite.mapper.FormationMapper;
import com.universite.mapper.MembreMapper;
import com.universite.repository.FormateurRepository;
import com.universite.repository.FormationRepository;
import com.universite.service.FormateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FormateurServiceImpl implements FormateurService {

    private final FormateurRepository formateurRepository;
    private final FormationRepository formationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FormateurFormationSummary> getFormations(Long formateurId) {
        Formateur formateur = findFormateur(formateurId);
        return mapFormations(formateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormateurFormationSummary> getFormationsForCurrentUser(String userEmail) {
        Formateur formateur = formateurRepository.findWithFormationsByUtilisateur_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("Profil formateur introuvable"));
        return mapFormations(formateur);
    }

    @Override
    @Transactional
    public MembreResponse assignFormations(Long formateurId, List<Long> formationIds) {
        Formateur formateur = findFormateur(formateurId);
        Set<Formation> formations = new HashSet<>();

        if (formationIds != null) {
            for (Long formationId : formationIds) {
                if (formationId == null) {
                    continue;
                }
                Formation formation = formationRepository.findById(formationId)
                        .orElseThrow(() -> new RuntimeException(
                                "Formation introuvable : " + formationId
                        ));
                formations.add(formation);
            }
        }

        formateur.setFormations(formations);
        return MembreMapper.fromFormateur(formateurRepository.save(formateur));
    }

    private List<FormateurFormationSummary> mapFormations(Formateur formateur) {
        return formateur.getFormations().stream()
                .sorted(Comparator.comparing(
                        FormationMapper::resolveTitre,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .map(formation -> FormateurFormationSummary.builder()
                        .id(formation.getId())
                        .titre(FormationMapper.resolveTitre(formation))
                        .slug(formation.getSlug())
                        .filiereId(
                                formation.getFiliere() != null
                                        ? formation.getFiliere().getId()
                                        : null
                        )
                        .filiereNom(
                                formation.getFiliere() != null
                                        ? formation.getFiliere().getNom()
                                        : null
                        )
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormateurFormationSummary> getModulesForCurrentUser(String userEmail) {
        return getFormationsForCurrentUser(userEmail);
    }

    private Formateur findFormateur(Long formateurId) {
        return formateurRepository.findById(formateurId)
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));
    }
}
