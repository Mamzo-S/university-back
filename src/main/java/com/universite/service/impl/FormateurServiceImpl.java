package com.universite.service.impl;

import com.universite.dto.FormateurFormationSummary;
import com.universite.dto.MembreResponse;
import com.universite.dto.parcours.FormationParcoursDto;
import com.universite.entity.Formateur;
import com.universite.entity.Formation;
import com.universite.entity.Utilisateur;
import com.universite.mapper.FormationMapper;
import com.universite.mapper.FormationParcoursMapper;
import com.universite.mapper.MembreMapper;
import com.universite.repository.FormateurRepository;
import com.universite.repository.FormationRepository;
import com.universite.service.FormateurService;
import com.universite.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FormateurServiceImpl implements FormateurService {

    private final FormateurRepository formateurRepository;
    private final FormationRepository formationRepository;
    private final FormationParcoursMapper formationParcoursMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<FormateurFormationSummary> getFormations(Long formateurId) {
        Formateur formateur = findFormateur(formateurId);
        if (formateur.getFormations() == null) {
            return List.of();
        }
        return formateur.getFormations().stream()
                .sorted(Comparator.comparing(
                        FormationMapper::resolveTitre,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .map(formation -> toSummary(formation, true, false))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormateurFormationSummary> getFormationsForCurrentUser(String userEmail) {
        Formateur formateur = formateurRepository.findWithFormationsByUtilisateur_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("Profil formateur introuvable"));
        return mapAccessibleFormations(formateur);
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
        Formateur saved = formateurRepository.save(formateur);
        notificationService.notifyFormateurModulesAssigned(saved, formations.size());
        return MembreMapper.fromFormateur(saved);
    }

    private List<FormateurFormationSummary> mapAccessibleFormations(Formateur formateur) {
        Map<Long, FormateurFormationSummary> byId = new HashMap<>();

        if (formateur.getFormations() != null) {
            for (Formation formation : formateur.getFormations()) {
                byId.put(formation.getId(), toSummary(formation, true, false));
            }
        }

        String trainerName = resolveTrainerName(formateur);
        if (!trainerName.isBlank()) {
            for (Formation formation : formationRepository.findAllWithFiliere()) {
                if (byId.containsKey(formation.getId())) {
                    FormateurFormationSummary existing = byId.get(formation.getId());
                    existing.setCree(isCreatedByTrainer(formation, trainerName));
                    continue;
                }

                if (isCreatedByTrainer(formation, trainerName)) {
                    byId.put(formation.getId(), toSummary(formation, false, true));
                }
            }
        }

        return byId.values().stream()
                .sorted(Comparator.comparing(
                        FormateurFormationSummary::getTitre,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .toList();
    }

    private FormateurFormationSummary toSummary(Formation formation, boolean assignee, boolean cree) {
        return FormateurFormationSummary.builder()
                .id(formation.getId())
                .titre(FormationMapper.resolveTitre(formation))
                .slug(formation.getSlug())
                .description(formation.getDescription())
                .imageUrl(formation.getImageUrl())
                .niveau(
                        formation.getNiveau() != null
                                ? formation.getNiveau().name()
                                : null
                )
                .typeFormation(formation.getTypeFormation())
                .subModuleCount(formationParcoursMapper.countSubModules(formation))
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
                .assignee(assignee)
                .cree(cree)
                .build();
    }

    private boolean isCreatedByTrainer(Formation formation, String trainerName) {
        FormationParcoursDto parcours = formationParcoursMapper.toDto(formation);
        return trainerName.equalsIgnoreCase(
                parcours.getTrainerName() != null ? parcours.getTrainerName().trim() : ""
        );
    }

    private String resolveTrainerName(Formateur formateur) {
        Utilisateur utilisateur = formateur.getUtilisateur();
        if (utilisateur == null) {
            return "";
        }
        return (utilisateur.getPrenom() + " " + utilisateur.getNom()).trim();
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
