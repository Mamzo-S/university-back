package com.universite.service.impl;

import com.universite.dto.PartenaireRequest;
import com.universite.dto.PartenaireResponse;
import com.universite.entity.Partenaire;
import com.universite.mapper.PartenaireMapper;
import com.universite.repository.PartenaireRepository;
import com.universite.repository.StageEtudiantRepository;
import com.universite.service.PartenaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartenaireServiceImpl implements PartenaireService {

    private final PartenaireRepository partenaireRepository;
    private final StageEtudiantRepository stageEtudiantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PartenaireResponse> getAll() {
        return partenaireRepository.findAllByOrderByNomAsc().stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PartenaireResponse getById(Long id) {
        return toResponseWithCount(findPartenaire(id));
    }

    @Override
    @Transactional
    public PartenaireResponse create(PartenaireRequest request) {
        Partenaire partenaire = applyRequest(new Partenaire(), request);
        return toResponseWithCount(partenaireRepository.save(partenaire));
    }

    @Override
    @Transactional
    public PartenaireResponse update(Long id, PartenaireRequest request) {
        Partenaire partenaire = findPartenaire(id);
        applyRequest(partenaire, request);
        return toResponseWithCount(partenaireRepository.save(partenaire));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Partenaire partenaire = findPartenaire(id);
        if (stageEtudiantRepository.countByPartenaire(partenaire) > 0) {
            throw new RuntimeException(
                    "Impossible de supprimer : des stages sont rattachés à ce partenaire"
            );
        }
        partenaireRepository.delete(partenaire);
    }

    private PartenaireResponse toResponseWithCount(Partenaire partenaire) {
        long stageCount = stageEtudiantRepository.countByPartenaire(partenaire);
        return PartenaireMapper.toResponse(partenaire, stageCount);
    }

    private Partenaire findPartenaire(Long id) {
        return partenaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partenaire introuvable"));
    }

    private Partenaire applyRequest(Partenaire partenaire, PartenaireRequest request) {
        if (request.getNom() == null || request.getNom().isBlank()) {
            throw new RuntimeException("Le nom du partenaire est obligatoire");
        }

        partenaire.setNom(request.getNom().trim());
        partenaire.setSecteur(trimOrNull(request.getSecteur()));
        partenaire.setEmail(trimOrNull(request.getEmail()));
        partenaire.setTelephone(trimOrNull(request.getTelephone()));
        partenaire.setAdresse(trimOrNull(request.getAdresse()));
        partenaire.setVille(trimOrNull(request.getVille()));
        partenaire.setPays(trimOrNull(request.getPays()));
        partenaire.setContactNom(trimOrNull(request.getContactNom()));
        partenaire.setContactFonction(trimOrNull(request.getContactFonction()));
        partenaire.setDescription(trimOrNull(request.getDescription()));

        if (request.getActif() != null) {
            partenaire.setActif(request.getActif());
        } else if (partenaire.getActif() == null) {
            partenaire.setActif(true);
        }

        if (request.getConventionCadre() != null) {
            partenaire.setConventionCadre(request.getConventionCadre());
        } else if (partenaire.getConventionCadre() == null) {
            partenaire.setConventionCadre(false);
        }

        return partenaire;
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
