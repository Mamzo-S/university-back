package com.universite.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universite.dto.parcours.FormationParcoursDto;
import com.universite.entity.Formation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FormationParcoursMapper {

    private final ObjectMapper objectMapper;

    public FormationParcoursDto toDto(Formation formation) {
        if (formation.getContenuParcours() == null || formation.getContenuParcours().isBlank()) {
            return emptyParcours();
        }
        try {
            FormationParcoursDto dto = objectMapper.readValue(
                    formation.getContenuParcours(),
                    FormationParcoursDto.class
            );
            if (dto.getSubModules() == null) {
                dto.setSubModules(emptyParcours().getSubModules());
            }
            if (dto.getProjectDeposits() == null) {
                dto.setProjectDeposits(emptyParcours().getProjectDeposits());
            }
            return dto;
        } catch (JsonProcessingException ex) {
            return emptyParcours();
        }
    }

    public void applyToFormation(Formation formation, FormationParcoursDto dto) {
        try {
            formation.setContenuParcours(objectMapper.writeValueAsString(normalize(dto)));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Impossible de sérialiser le parcours pédagogique");
        }
    }

    public int countSubModules(Formation formation) {
        return toDto(formation).getSubModules().size();
    }

    public static FormationParcoursDto emptyParcours() {
        return FormationParcoursDto.builder().build();
    }

    private FormationParcoursDto normalize(FormationParcoursDto dto) {
        if (dto.getSubModules() == null) {
            dto.setSubModules(emptyParcours().getSubModules());
        }
        if (dto.getProjectDeposits() == null) {
            dto.setProjectDeposits(emptyParcours().getProjectDeposits());
        }
        return dto;
    }
}
