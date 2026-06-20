package com.universite.service;

import com.universite.dto.PromotionRequest;
import com.universite.dto.PromotionResponse;

import java.util.List;

public interface PromotionService {

    List<PromotionResponse> listAll(Long formationId);

    PromotionResponse getById(Long id);

    PromotionResponse create(PromotionRequest request);

    PromotionResponse update(Long id, PromotionRequest request);

    void delete(Long id);
}
