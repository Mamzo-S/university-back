package com.universite.service.impl;

import com.universite.dto.CareerStatsResponse;
import com.universite.dto.StageRequest;
import com.universite.dto.StageResponse;
import com.universite.entity.Etudiant;
import com.universite.entity.Partenaire;
import com.universite.entity.StageEtudiant;
import com.universite.entity.StatutStage;
import com.universite.mapper.StageMapper;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.PartenaireRepository;
import com.universite.repository.StageEtudiantRepository;
import com.universite.service.StageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StageServiceImpl implements StageService {

    private final StageEtudiantRepository stageEtudiantRepository;
    private final EtudiantRepository etudiantRepository;
    private final PartenaireRepository partenaireRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StageResponse> getAll() {
        return stageEtudiantRepository.findAllWithDetails().stream()
                .map(StageMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StageResponse> getForCurrentEtudiant(String userEmail) {
        return stageEtudiantRepository.findByEtudiantEmail(userEmail).stream()
                .map(StageMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StageResponse getById(Long id) {
        return StageMapper.toResponse(findStage(id));
    }

    @Override
    @Transactional
    public StageResponse create(StageRequest request) {
        StageEtudiant stage = applyRequest(new StageEtudiant(), request);
        return StageMapper.toResponse(stageEtudiantRepository.save(stage));
    }

    @Override
    @Transactional
    public StageResponse update(Long id, StageRequest request) {
        StageEtudiant stage = findStage(id);
        applyRequest(stage, request);
        return StageMapper.toResponse(stageEtudiantRepository.save(stage));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        stageEtudiantRepository.delete(findStage(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CareerStatsResponse getCareerStats() {
        long active = stageEtudiantRepository.countByStatut(StatutStage.EN_COURS);
        long pendingConventions = stageEtudiantRepository.countByConventionSigneeFalse();
        long terminated = stageEtudiantRepository.countByStatut(StatutStage.TERMINE);

        return CareerStatsResponse.builder()
                .internships(stageEtudiantRepository.count())
                .partners(partenaireRepository.countByActifTrue())
                .activeInternships(active)
                .pendingConventions(pendingConventions)
                .employed(terminated)
                .selfEmployed(0)
                .build();
    }

    private StageEtudiant findStage(Long id) {
        return stageEtudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stage introuvable"));
    }

    private StageEtudiant applyRequest(StageEtudiant stage, StageRequest request) {
        if (request.getEtudiantId() == null) {
            throw new RuntimeException("L'étudiant est obligatoire");
        }
        if (request.getPartenaireId() == null) {
            throw new RuntimeException("Le partenaire est obligatoire");
        }

        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));
        Partenaire partenaire = partenaireRepository.findById(request.getPartenaireId())
                .orElseThrow(() -> new RuntimeException("Partenaire introuvable"));

        stage.setEtudiant(etudiant);
        stage.setPartenaire(partenaire);
        stage.setSujet(trimOrNull(request.getSujet()));
        stage.setDescription(trimOrNull(request.getDescription()));
        stage.setDateDebut(parseDate(request.getDateDebut()));
        stage.setDateFin(parseDate(request.getDateFin()));
        stage.setStatut(parseStatut(request.getStatut()));
        stage.setTuteurEntreprise(trimOrNull(request.getTuteurEntreprise()));
        stage.setTuteurUniversite(trimOrNull(request.getTuteurUniversite()));

        if (request.getConventionSignee() != null) {
            stage.setConventionSignee(request.getConventionSignee());
        } else if (stage.getConventionSignee() == null) {
            stage.setConventionSignee(false);
        }

        stage.setCommentaire(trimOrNull(request.getCommentaire()));
        return stage;
    }

    private StatutStage parseStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            return StatutStage.EN_ATTENTE;
        }
        try {
            return StatutStage.valueOf(statut.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Statut de stage invalide : " + statut);
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Date invalide (format attendu : AAAA-MM-JJ)");
        }
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
