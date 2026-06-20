package com.universite.service.impl;

import com.universite.dto.AnneeAcademiqueRequest;
import com.universite.dto.AnneeAcademiqueResponse;
import com.universite.entity.AnneeAcademique;
import com.universite.mapper.AnneeAcademiqueMapper;
import com.universite.repository.AnneeAcademiqueRepository;
import com.universite.repository.PromotionRepository;
import com.universite.service.AnneeAcademiqueService;
import com.universite.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnneeAcademiqueServiceImpl implements AnneeAcademiqueService {

    private final AnneeAcademiqueRepository anneeAcademiqueRepository;
    private final PromotionRepository promotionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AnneeAcademiqueResponse> getAll() {
        return anneeAcademiqueRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        AnneeAcademique::getTitre,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ).reversed())
                .map(AnneeAcademiqueMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AnneeAcademiqueResponse getById(Long id) {
        return AnneeAcademiqueMapper.toResponse(findAnnee(id));
    }

    @Override
    @Transactional
    public AnneeAcademiqueResponse create(AnneeAcademiqueRequest request) {
        String titre = requireTitre(request);
        String slug = SlugUtils.resolveSlug(
                request.getSlug(),
                titre,
                anneeAcademiqueRepository::existsBySlug
        );

        AnneeAcademique annee = AnneeAcademique.builder()
                .titre(titre)
                .slug(slug)
                .description(trimOrNull(request.getDescription()))
                .build();

        return AnneeAcademiqueMapper.toResponse(anneeAcademiqueRepository.save(annee));
    }

    @Override
    @Transactional
    public AnneeAcademiqueResponse update(Long id, AnneeAcademiqueRequest request) {
        AnneeAcademique annee = findAnnee(id);
        String titre = requireTitre(request);
        String slug = SlugUtils.resolveSlug(
                request.getSlug() != null ? request.getSlug() : annee.getSlug(),
                titre,
                candidate -> anneeAcademiqueRepository.existsBySlugAndIdNot(candidate, id)
        );

        annee.setTitre(titre);
        annee.setSlug(slug);
        annee.setDescription(trimOrNull(request.getDescription()));

        return AnneeAcademiqueMapper.toResponse(anneeAcademiqueRepository.save(annee));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!promotionRepository.findByAnneeAcademiqueRefId(id).isEmpty()) {
            throw new RuntimeException(
                    "Impossible de supprimer : des promotions utilisent cette année académique"
            );
        }
        anneeAcademiqueRepository.delete(findAnnee(id));
    }

    private AnneeAcademique findAnnee(Long id) {
        return anneeAcademiqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Année académique introuvable"));
    }

    private String requireTitre(AnneeAcademiqueRequest request) {
        if (request.getTitre() == null || request.getTitre().isBlank()) {
            throw new RuntimeException("Le titre de l'année académique est obligatoire");
        }
        return request.getTitre().trim();
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
