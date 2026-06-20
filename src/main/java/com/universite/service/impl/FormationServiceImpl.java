package com.universite.service.impl;

import com.universite.dto.FormationRequest;
import com.universite.dto.FormationResponse;
import com.universite.dto.parcours.FormationParcoursDto;
import com.universite.entity.Filiere;
import com.universite.entity.Formateur;
import com.universite.entity.Formation;
import com.universite.entity.RoleName;
import com.universite.entity.Utilisateur;
import com.universite.mapper.FormationMapper;
import com.universite.mapper.FormationParcoursMapper;
import com.universite.repository.CoursRepository;
import com.universite.repository.FiliereRepository;
import com.universite.repository.FormateurRepository;
import com.universite.repository.FormationRepository;
import com.universite.repository.PromotionRepository;
import com.universite.repository.UtilisateurRepository;
import com.universite.service.FormationAccessService;
import com.universite.service.FormationService;
import com.universite.util.NiveauEtudeParser;
import com.universite.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FormationServiceImpl implements FormationService {

    private final FormationRepository formationRepository;
    private final FiliereRepository filiereRepository;
    private final PromotionRepository promotionRepository;
    private final CoursRepository coursRepository;
    private final FormateurRepository formateurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FormationParcoursMapper formationParcoursMapper;
    private final FormationAccessService formationAccessService;

    @Override
    @Transactional
    public FormationResponse create(FormationRequest request, String userEmail) {
        String titre = resolveTitreInput(request);
        String slug = SlugUtils.resolveSlug(
                request.getSlug(),
                titre,
                formationRepository::existsBySlug
        );

        Formation formation = applyFields(new Formation(), request, titre, slug);
        formation = formationRepository.save(formation);
        linkFormationToFormateurIfNeeded(formation, request, userEmail);
        return FormationMapper.toResponse(formation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormationResponse> getAll() {
        Map<Long, List<String>> formateursByFormation = loadFormateursByFormation();

        return formationRepository.findAllWithFiliere().stream()
                .sorted(Comparator.comparing(
                        FormationMapper::resolveTitre,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .map(formation -> {
                    FormationResponse response = FormationMapper.toResponse(formation);
                    response.setFormateurNoms(
                            formateursByFormation.getOrDefault(formation.getId(), List.of())
                    );
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FormationResponse getById(Long id, String userEmail) {
        Formation formation = findFormation(id);
        formationAccessService.assertCanReadFormation(formation, userEmail);
        return FormationMapper.toResponse(formation);
    }

    @Override
    @Transactional(readOnly = true)
    public FormationResponse getBySlug(String slug, String userEmail) {
        Formation formation = findFormationBySlugWithFiliere(slug);
        formationAccessService.assertCanReadFormation(formation, userEmail);
        return FormationMapper.toResponse(formation);
    }

    @Override
    @Transactional
    public FormationResponse update(Long id, FormationRequest request, String userEmail) {
        Formation formation = findFormation(id);
        formationAccessService.assertCanManageFormation(formation, userEmail);

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
    public void delete(Long id, String userEmail) {
        Formation formation = findFormation(id);
        formationAccessService.assertCanDeleteFormation(formation, userEmail);
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

    private Formation findFormationBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new RuntimeException("Slug de formation invalide");
        }
        return formationRepository.findBySlug(slug.trim())
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
    }

    private Formation findFormationBySlugWithFiliere(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new RuntimeException("Slug de formation invalide");
        }
        return formationRepository.findBySlugWithFiliere(slug.trim())
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

    private void linkFormationToFormateurIfNeeded(
            Formation formation,
            FormationRequest request,
            String userEmail
    ) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail).orElse(null);
        if (utilisateur == null || !utilisateur.hasRole(RoleName.FORMATEUR)) {
            return;
        }

        Formateur formateur = formateurRepository.findWithFormationsByUtilisateur_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("Profil formateur introuvable"));

        if (formateur.getFormations() == null) {
            formateur.setFormations(new HashSet<>());
        }
        formateur.getFormations().add(formation);
        formateurRepository.save(formateur);

        FormationParcoursDto parcours = FormationParcoursMapper.emptyParcours();
        parcours.setTrainerName(resolveFormateurNom(formateur));
        parcours.setDuration(trimOrNull(request.getDuration()));
        parcours.setSessionUrl(trimOrNull(request.getSessionUrl()));
        formationParcoursMapper.applyToFormation(formation, parcours);
        formationRepository.save(formation);
    }
}
