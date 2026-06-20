package com.universite.service;

import com.universite.dto.FormationRequest;
import com.universite.dto.FormationResponse;

import java.util.List;

public interface FormationService {

    FormationResponse create(FormationRequest request, String userEmail);

    List<FormationResponse> getAll();

    FormationResponse getById(Long id, String userEmail);

    FormationResponse getBySlug(String slug, String userEmail);

    FormationResponse update(Long id, FormationRequest request, String userEmail);

    void delete(Long id, String userEmail);
}
