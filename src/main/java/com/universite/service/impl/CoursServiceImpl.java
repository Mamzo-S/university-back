package com.universite.service.impl;

import com.universite.dto.CoursRequest;
import com.universite.dto.CoursResponse;
import com.universite.entity.Cours;
import com.universite.entity.Formation;
import com.universite.repository.CoursRepository;
import com.universite.repository.FormationRepository;
import com.universite.service.CoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursServiceImpl implements CoursService {

    private final CoursRepository coursRepository;
    private final FormationRepository formationRepository;

    @Override
    public CoursResponse create(CoursRequest request) {
        if (coursRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Un cours avec ce code existe déjà");
        }

        Formation formation = formationRepository.findById(request.getFormationId())
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        Cours cours = Cours.builder()
                .code(request.getCode())
                .nom(request.getNom())
                .semestre(request.getSemestre())
                .coefficient(request.getCoefficient())
                .formation(formation)
                .build();

        coursRepository.save(cours);
        return mapToResponse(cours);
    }

    @Override
    public CoursResponse update(Long id, CoursRequest request) {
        Cours cours = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        if (!cours.getCode().equals(request.getCode())
                && coursRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Un cours avec ce code existe déjà");
        }

        Formation formation = formationRepository.findById(request.getFormationId())
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        cours.setCode(request.getCode());
        cours.setNom(request.getNom());
        cours.setSemestre(request.getSemestre());
        cours.setCoefficient(request.getCoefficient());
        cours.setFormation(formation);

        coursRepository.save(cours);
        return mapToResponse(cours);
    }

    @Override
    public CoursResponse getById(Long id) {
        Cours cours = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));
        return mapToResponse(cours);
    }

    @Override
    public List<CoursResponse> getAll() {
        return coursRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<CoursResponse> getByFormation(Long formationId) {
        return coursRepository.findByFormationId(formationId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        coursRepository.deleteById(id);
    }

    private CoursResponse mapToResponse(Cours cours) {
        return CoursResponse.builder()
                .id(cours.getId())
                .code(cours.getCode())
                .nom(cours.getNom())
                .semestre(cours.getSemestre())
                .coefficient(cours.getCoefficient())
                .formationId(cours.getFormation().getId())
                .formationNom(cours.getFormation().getNom())
                .build();
    }
}
