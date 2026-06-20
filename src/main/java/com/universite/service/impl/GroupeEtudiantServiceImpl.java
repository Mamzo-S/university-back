package com.universite.service.impl;

import com.universite.dto.GroupeEtudiantRequest;
import com.universite.dto.GroupeEtudiantResponse;
import com.universite.entity.GroupeEtudiant;
import com.universite.entity.Promotion;
import com.universite.mapper.GroupeEtudiantMapper;
import com.universite.repository.GroupeEtudiantRepository;
import com.universite.repository.PromotionRepository;
import com.universite.service.GroupeEtudiantService;
import com.universite.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupeEtudiantServiceImpl implements GroupeEtudiantService {

    private final GroupeEtudiantRepository groupeEtudiantRepository;
    private final PromotionRepository promotionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GroupeEtudiantResponse> listAll(Long promotionId, Long formationId, Long filiereId) {
        List<GroupeEtudiant> groupes;

        if (promotionId != null) {
            groupes = groupeEtudiantRepository.findByPromotionId(promotionId);
        } else if (formationId != null) {
            groupes = groupeEtudiantRepository.findByPromotionFormationId(formationId);
        } else if (filiereId != null) {
            groupes = groupeEtudiantRepository.findByPromotionFormationFiliereId(filiereId);
        } else {
            groupes = groupeEtudiantRepository.findAll();
        }

        return groupes.stream()
                .sorted(Comparator.comparing(
                        GroupeEtudiant::getTitre,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .map(GroupeEtudiantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupeEtudiantResponse getById(Long id) {
        return GroupeEtudiantMapper.toResponse(findGroupe(id));
    }

    @Override
    @Transactional
    public GroupeEtudiantResponse create(GroupeEtudiantRequest request) {
        String titre = requireTitre(request);
        Promotion promotion = resolvePromotion(request.getPromotionId());
        String slug = SlugUtils.resolveSlug(
                request.getSlug(),
                titre,
                groupeEtudiantRepository::existsBySlug
        );

        GroupeEtudiant groupe = GroupeEtudiant.builder()
                .titre(titre)
                .slug(slug)
                .description(trimOrNull(request.getDescription()))
                .promotion(promotion)
                .build();

        return GroupeEtudiantMapper.toResponse(groupeEtudiantRepository.save(groupe));
    }

    @Override
    @Transactional
    public GroupeEtudiantResponse update(Long id, GroupeEtudiantRequest request) {
        GroupeEtudiant groupe = findGroupe(id);
        String titre = requireTitre(request);
        String slug = SlugUtils.resolveSlug(
                request.getSlug() != null ? request.getSlug() : groupe.getSlug(),
                titre,
                candidate -> groupeEtudiantRepository.existsBySlugAndIdNot(candidate, id)
        );

        if (request.getPromotionId() != null) {
            groupe.setPromotion(resolvePromotion(request.getPromotionId()));
        }

        groupe.setTitre(titre);
        groupe.setSlug(slug);
        groupe.setDescription(trimOrNull(request.getDescription()));

        return GroupeEtudiantMapper.toResponse(groupeEtudiantRepository.save(groupe));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        GroupeEtudiant groupe = findGroupe(id);
        if (groupe.getEtudiants() != null && !groupe.getEtudiants().isEmpty()) {
            throw new RuntimeException(
                    "Impossible de supprimer : des étudiants sont rattachés à ce groupe"
            );
        }
        groupeEtudiantRepository.delete(groupe);
    }

    private GroupeEtudiant findGroupe(Long id) {
        return groupeEtudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Groupe d'étudiants introuvable"));
    }

    private Promotion resolvePromotion(Long promotionId) {
        if (promotionId == null) {
            throw new RuntimeException("La promotion est obligatoire pour un groupe d'étudiants");
        }
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion introuvable"));
    }

    private String requireTitre(GroupeEtudiantRequest request) {
        if (request.getTitre() == null || request.getTitre().isBlank()) {
            throw new RuntimeException("Le titre du groupe est obligatoire");
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
