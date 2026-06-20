package com.universite.service.impl;

import com.universite.dto.PromotionRequest;
import com.universite.dto.PromotionResponse;
import com.universite.entity.AnneeAcademique;
import com.universite.entity.EmploiDuTemps;
import com.universite.entity.Promotion;
import com.universite.mapper.PromotionMapper;
import com.universite.repository.AnneeAcademiqueRepository;
import com.universite.repository.EmploiDuTempsRepository;
import com.universite.repository.PromotionRepository;
import com.universite.repository.SeanceRepository;
import com.universite.service.PromotionService;
import com.universite.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final AnneeAcademiqueRepository anneeAcademiqueRepository;
    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final SeanceRepository seanceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> listAll(Long formationId) {
        List<Promotion> promotions = formationId != null
                ? promotionRepository.findByFormationId(formationId)
                : promotionRepository.findAll();

        return promotions.stream()
                .sorted(Comparator.comparing(
                        PromotionMapper::resolveTitre,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .map(PromotionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getById(Long id) {
        return PromotionMapper.toResponse(findPromotion(id));
    }

    @Override
    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        String titre = resolveTitreInput(request);
        String slug = SlugUtils.resolveSlug(
                request.getSlug(),
                titre,
                promotionRepository::existsBySlug
        );

        AnneeAcademique annee = resolveAnnee(request);

        Promotion promotion = Promotion.builder()
                .titre(titre)
                .nom(titre)
                .slug(slug)
                .description(trimOrNull(request.getDescription()))
                .anneeAcademique(resolveAnneeLabel(annee, request, titre))
                .anneeAcademiqueRef(annee)
                .build();

        promotion = promotionRepository.save(promotion);

        emploiDuTempsRepository.save(EmploiDuTemps.builder()
                .promotion(promotion)
                .libelle("EDT — " + titre)
                .publie(false)
                .build());

        return PromotionMapper.toResponse(promotion);
    }

    @Override
    @Transactional
    public PromotionResponse update(Long id, PromotionRequest request) {
        Promotion promotion = findPromotion(id);
        String titre = resolveTitreInput(request);
        String slug = SlugUtils.resolveSlug(
                request.getSlug() != null ? request.getSlug() : promotion.getSlug(),
                titre,
                candidate -> promotionRepository.existsBySlugAndIdNot(candidate, id)
        );

        AnneeAcademique annee = resolveAnnee(request);
        promotion.setTitre(titre);
        promotion.setNom(titre);
        promotion.setSlug(slug);
        promotion.setDescription(trimOrNull(request.getDescription()));
        promotion.setAnneeAcademiqueRef(annee);
        promotion.setAnneeAcademique(resolveAnneeLabel(annee, request, titre));

        return PromotionMapper.toResponse(promotionRepository.save(promotion));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Promotion promotion = findPromotion(id);
        if (promotion.getEtudiants() != null && !promotion.getEtudiants().isEmpty()) {
            throw new RuntimeException(
                    "Impossible de supprimer : des étudiants sont rattachés à cette promotion"
            );
        }
        if (!seanceRepository.findByEmploiDuTemps_Promotion_Id(id).isEmpty()) {
            throw new RuntimeException(
                    "Impossible de supprimer : des séances sont planifiées pour cette promotion"
            );
        }
        promotionRepository.delete(promotion);
    }

    private Promotion findPromotion(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion introuvable"));
    }

    private AnneeAcademique resolveAnnee(PromotionRequest request) {
        if (request.getAnneeAcademiqueId() != null) {
            return anneeAcademiqueRepository.findById(request.getAnneeAcademiqueId())
                    .orElseThrow(() -> new RuntimeException("Année académique introuvable"));
        }
        return null;
    }

    private String resolveAnneeLabel(AnneeAcademique annee, PromotionRequest request, String fallback) {
        if (annee != null) {
            return annee.getTitre();
        }
        if (request.getAnneeAcademique() != null && !request.getAnneeAcademique().isBlank()) {
            return request.getAnneeAcademique().trim();
        }
        return fallback;
    }

    private String resolveTitreInput(PromotionRequest request) {
        String titre = request.getTitre();
        if (titre == null || titre.isBlank()) {
            titre = request.getNom();
        }
        if (titre == null || titre.isBlank()) {
            throw new RuntimeException("Le titre de la promotion est obligatoire");
        }
        return titre.trim();
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
