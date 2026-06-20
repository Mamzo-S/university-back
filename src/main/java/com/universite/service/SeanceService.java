package com.universite.service;

import com.universite.dto.SeanceRequest;
import com.universite.dto.SeanceResponse;

import java.util.List;

public interface SeanceService {

    List<SeanceResponse> listByPromotion(Long promotionId);

    List<SeanceResponse> listAll();

    List<SeanceResponse> listByFormation(Long formationId);

    List<SeanceResponse> listForCurrentUser(String userEmail);

    SeanceResponse create(SeanceRequest request);

    SeanceResponse update(Long id, SeanceRequest request);

    void delete(Long id);
}
