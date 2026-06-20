package com.universite.service;

import com.universite.dto.parcours.FormationParcoursDto;

public interface FormationParcoursService {

    FormationParcoursDto getParcours(Long formationId, String userEmail);

    FormationParcoursDto getParcoursBySlug(String slug, String userEmail);

    FormationParcoursDto updateParcours(Long formationId, FormationParcoursDto parcours, String userEmail);
}
