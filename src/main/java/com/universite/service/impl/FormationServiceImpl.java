package com.universite.service.impl;

import com.universite.dto.FormationRequest;
import com.universite.dto.FormationResponse;
import com.universite.entity.Filiere;
import com.universite.entity.Formation;
import com.universite.mapper.FormationMapper;
import com.universite.repository.CoursRepository;
import com.universite.repository.FiliereRepository;
import com.universite.repository.FormationRepository;
import com.universite.repository.PromotionRepository;
import com.universite.service.FormationService;
import com.universite.util.NiveauEtudeParser;
import com.universite.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormationServiceImpl implements FormationService {

    private final FormationRepository formationRepository;
    private final FiliereRepository filiereRepository;
    private final PromotionRepository promotionRepository;
    private final CoursRepository coursRepository;

    @Override
    @Transactional
    public FormationResponse create(FormationRequest request) {
        String titre = resolveTitreInput(request);
        String slug = SlugUtils.resolveSlug(
                request.getSlug(),
                titre,
                formationRepository::existsBySlug
        );

        Formation formation = applyFields(new Formation(), request, titre, slug);
        return FormationMapper.toResponse(formationRepository.save(formation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormationResponse> getAll() {
        return formationRepository.findAllWithFiliere().stream()
                .sorted(Comparator.comparing(
                        FormationMapper::resolveTitre,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .map(FormationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FormationResponse getById(Long id) {
        return FormationMapper.toResponse(findFormation(id));
    }

    @Override
    @Transactional
    public FormationResponse update(Long id, FormationRequest request) {
        Formation formation = findFormation(id);
        String titre = resolveTitreInput(request);
        String slug = SlugUtils.resolveSlug(
                request.getSlug() != null ? request.getSlug() : formation.getSlug(),
                titre,
                candidate -> formationRepository.existsBySlugAndIdNot(candidate, id)
        );

        applyFields(formation, request, titre, slug);
        return FormationMapper.toResponse(formationRepository.save(formation));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Formation formation = findFormation(id);
        if (!promotionRepository.findByFormationId(id).isEmpty()) {
            throw new RuntimeException(
                    "Impossible de supprimer : des promotions sont rattachées à cette formation"
            );
        }
        if (!coursRepository.findByFormationId(id).isEmpty()) {
            throw new RuntimeException(
                    "Impossible de supprimer : des modules sont rattachés à cette formation"
            );
        }
        formationRepository.delete(formation);
    }

    private Formation findFormation(Long id) {
        return formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
    }

    private Formation applyFields(
            Formation formation,
            FormationRequest request,
            String titre,
            String slug
    ) {
        Filiere filiere = null;
        if (request.getFiliereId() != null) {
            filiere = filiereRepository.findById(request.getFiliereId())
                    .orElseThrow(() -> new RuntimeException("Filière introuvable"));
        }

        formation.setTitre(titre);
        formation.setNom(titre);
        formation.setSlug(slug);
        formation.setDescription(trimOrNull(request.getDescription()));
        formation.setImageUrl(trimOrNull(request.getImageUrl()));
        formation.setNiveau(NiveauEtudeParser.parse(request.getNiveau()));
        formation.setTypeFormation(trimOrNull(request.getTypeFormation()));
        formation.setTypeFinancement(trimOrNull(request.getTypeFinancement()));
        formation.setDateDebut(parseDate(request.getDateDebut()));
        formation.setDateFin(parseDate(request.getDateFin()));
        formation.setMontant(request.getMontant());
        formation.setFiliere(filiere);
        return formation;
    }

    private String resolveTitreInput(FormationRequest request) {
        String titre = request.getTitre();
        if (titre == null || titre.isBlank()) {
            titre = request.getNom();
        }
        if (titre == null || titre.isBlank()) {
            throw new RuntimeException("Le titre de la formation est obligatoire");
        }
        return titre.trim();
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Date invalide (format attendu : AAAA-MM-JJ)");
        }
    }
}
