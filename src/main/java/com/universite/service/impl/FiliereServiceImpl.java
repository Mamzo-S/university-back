package com.universite.service.impl;

import com.universite.dto.FiliereDetailResponse;
import com.universite.dto.FiliereRequest;
import com.universite.dto.FiliereResponse;
import com.universite.entity.Filiere;
import com.universite.mapper.FiliereMapper;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.FiliereRepository;
import com.universite.repository.FormationRepository;
import com.universite.service.FiliereService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FiliereServiceImpl implements FiliereService {

    private final FiliereRepository filiereRepository;
    private final FormationRepository formationRepository;
    private final EtudiantRepository etudiantRepository;

    @Override
    @Transactional
    public FiliereResponse create(FiliereRequest request) {
        String nom = requireNom(request);
        Filiere filiere = Filiere.builder()
                .nom(nom)
                .description(trimOrNull(request.getDescription()))
                .build();
        return FiliereMapper.toResponse(filiereRepository.save(filiere), 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FiliereResponse> getAll() {
        return filiereRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        Filiere::getNom,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .map(this::toResponseWithCounts)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FiliereResponse getById(Long id) {
        return toResponseWithCounts(findFiliere(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FiliereDetailResponse getDetail(Long id) {
        Filiere filiere = findFiliere(id);
        return FiliereMapper.toDetailResponse(
                filiere,
                formationRepository.findByFiliereId(id),
                etudiantRepository.findByFiliereId(id)
        );
    }

    @Override
    @Transactional
    public FiliereResponse update(Long id, FiliereRequest request) {
        Filiere filiere = findFiliere(id);
        filiere.setNom(requireNom(request));
        filiere.setDescription(trimOrNull(request.getDescription()));
        return toResponseWithCounts(filiereRepository.save(filiere));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (formationRepository.countByFiliereId(id) > 0) {
            throw new RuntimeException(
                    "Impossible de supprimer : des modules (formations) sont rattachés à cette filière"
            );
        }
        filiereRepository.delete(findFiliere(id));
    }

    private FiliereResponse toResponseWithCounts(Filiere filiere) {
        long moduleCount = formationRepository.countByFiliereId(filiere.getId());
        long etudiantCount = etudiantRepository.countByFiliereId(filiere.getId());
        return FiliereMapper.toResponse(filiere, moduleCount, etudiantCount);
    }

    private Filiere findFiliere(Long id) {
        return filiereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filière introuvable"));
    }

    private String requireNom(FiliereRequest request) {
        if (request.getNom() == null || request.getNom().isBlank()) {
            throw new RuntimeException("Le nom de la filière est obligatoire");
        }
        return request.getNom().trim();
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
