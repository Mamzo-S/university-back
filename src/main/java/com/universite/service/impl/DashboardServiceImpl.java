package com.universite.service.impl;

import com.universite.dto.AdminDashboardResponse;
import com.universite.dto.StatistiqueDTO;
import com.universite.dto.TrainingDashboardResponse;
import com.universite.dto.TrainingFormationSummary;
import com.universite.entity.Formateur;
import com.universite.entity.Formation;
import com.universite.entity.Utilisateur;
import com.universite.mapper.FormationMapper;
import com.universite.repository.EmploiDuTempsRepository;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.FiliereRepository;
import com.universite.repository.FormateurRepository;
import com.universite.repository.FormationRepository;
import com.universite.repository.PersonnelRepository;
import com.universite.repository.PromotionRepository;
import com.universite.repository.UtilisateurRepository;
import com.universite.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EtudiantRepository etudiantRepository;
    private final FormationRepository formationRepository;
    private final FormateurRepository formateurRepository;
    private final PersonnelRepository personnelRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FiliereRepository filiereRepository;
    private final PromotionRepository promotionRepository;
    private final EmploiDuTempsRepository emploiDuTempsRepository;

    @Override
    @Transactional(readOnly = true)
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

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminStats() {
        return AdminDashboardResponse.builder()
                .users(utilisateurRepository.count())
                .students(etudiantRepository.count())
                .formations(formationRepository.count())
                .trainers(formateurRepository.count())
                .personnel(personnelRepository.count())
                .filieres(filiereRepository.count())
                .promotions(promotionRepository.count())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingDashboardResponse getTrainingStats() {
        Map<Long, List<String>> formateursByFormation = loadFormateursByFormation();

        List<TrainingFormationSummary> formationSummaries = formationRepository.findAllWithFiliere().stream()
                .sorted(Comparator.comparing(
                        FormationMapper::resolveTitre,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .map(formation -> toFormationSummary(formation, formateursByFormation))
                .toList();

        return TrainingDashboardResponse.builder()
                .formations(formationRepository.count())
                .schedules(emploiDuTempsRepository.count())
                .trainers(formateurRepository.count())
                .students(etudiantRepository.count())
                .filieres(filiereRepository.count())
                .promotions(promotionRepository.count())
                .formationSummaries(formationSummaries)
                .build();
    }

    private TrainingFormationSummary toFormationSummary(
            Formation formation,
            Map<Long, List<String>> formateursByFormation
    ) {
        return TrainingFormationSummary.builder()
                .id(formation.getId())
                .titre(FormationMapper.resolveTitre(formation))
                .niveau(
                        formation.getNiveau() != null
                                ? formation.getNiveau().name()
                                : null
                )
                .typeFormation(formation.getTypeFormation())
                .filiereNom(
                        formation.getFiliere() != null
                                ? formation.getFiliere().getNom()
                                : null
                )
                .effectif(countFormationEffectif(formation))
                .formateurNoms(
                        formateursByFormation.getOrDefault(formation.getId(), List.of())
                )
                .build();
    }

    private long countFormationEffectif(Formation formation) {
        Long filiereId = formation.getFiliere() != null
                ? formation.getFiliere().getId()
                : null;

        if (formation.getNiveau() != null && filiereId != null) {
            return etudiantRepository.countByFormationScope(
                    formation.getId(),
                    filiereId,
                    formation.getNiveau()
            );
        }

        return etudiantRepository.countByFormationId(formation.getId());
    }

    private Map<Long, List<String>> loadFormateursByFormation() {
        Map<Long, List<String>> formateursByFormation = new HashMap<>();

        for (Formateur formateur : formateurRepository.findAllWithFormations()) {
            String formateurNom = resolveFormateurNom(formateur);
            if (formateurNom.isBlank() || formateur.getFormations() == null) {
                continue;
            }

            for (Formation formation : formateur.getFormations()) {
                if (formation.getId() == null) {
                    continue;
                }
                formateursByFormation
                        .computeIfAbsent(formation.getId(), ignored -> new ArrayList<>())
                        .add(formateurNom);
            }
        }

        formateursByFormation.values().forEach(names ->
                names.sort(String.CASE_INSENSITIVE_ORDER)
        );

        return formateursByFormation;
    }

    private String resolveFormateurNom(Formateur formateur) {
        Utilisateur utilisateur = formateur.getUtilisateur();
        if (utilisateur == null) {
            return "";
        }
        return (utilisateur.getPrenom() + " " + utilisateur.getNom()).trim();
    }
}
