package com.universite.service;

import com.universite.dto.EmploiDuTempsResponse;
import com.universite.dto.SeanceResponse;
import com.universite.entity.Promotion;

import java.util.List;

public interface EmploiDuTempsService {

    EmploiDuTempsResponse getByPromotion(Long promotionId);

    List<SeanceResponse> getForCurrentEtudiant(String userEmail);

    List<SeanceResponse> getForEtudiant(Long etudiantId, String requesterEmail);

    List<SeanceResponse> getSeancesForCurrentFormateur(String userEmail);

    EmploiDuTempsResponse ensureForPromotion(Promotion promotion);
}
