package com.universite.service.impl;

import com.universite.dto.FiliereRequest;
import com.universite.dto.FiliereResponse;
import com.universite.entity.Filiere;
import com.universite.repository.FiliereRepository;
import com.universite.service.FiliereService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FiliereServiceImpl implements FiliereService {

    private final FiliereRepository filiereRepository;

    @Override
    public FiliereResponse create(FiliereRequest request) {

        Filiere filiere = Filiere.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .build();

        filiereRepository.save(filiere);

        return mapToResponse(filiere);
    }

    @Override
    public List<FiliereResponse> getAll() {

        return filiereRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public FiliereResponse getById(Long id) {

        Filiere filiere = filiereRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Filière introuvable")
                );

        return mapToResponse(filiere);
    }

    @Override
    public FiliereResponse update(Long id, FiliereRequest request) {

        Filiere filiere = filiereRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Filière introuvable")
                );

        filiere.setNom(request.getNom());
        filiere.setDescription(request.getDescription());

        filiereRepository.save(filiere);

        return mapToResponse(filiere);
    }

    @Override
    public void delete(Long id) {

        filiereRepository.deleteById(id);
    }

    private FiliereResponse mapToResponse(Filiere filiere) {

        return FiliereResponse.builder()
                .id(filiere.getId())
                .nom(filiere.getNom())
                .description(filiere.getDescription())
                .build();
    }
}
