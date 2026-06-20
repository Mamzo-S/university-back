package com.universite.service;

import com.universite.dto.FormateurFormationSummary;
import com.universite.dto.MembreResponse;
import com.universite.entity.Formation;

import java.util.List;

public interface FormateurService {

    List<FormateurFormationSummary> getFormations(Long formateurId);

    List<FormateurFormationSummary> getFormationsForCurrentUser(String userEmail);

    List<FormateurFormationSummary> getModulesForCurrentUser(String userEmail);

    MembreResponse assignFormations(Long formateurId, List<Long> formationIds);
}
