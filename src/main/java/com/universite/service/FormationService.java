package com.universite.service;

import com.universite.dto.FormationRequest;
import com.universite.dto.FormationResponse;

import java.util.List;

public interface FormationService {

    FormationResponse create(FormationRequest request);

    List<FormationResponse> getAll();

    FormationResponse getById(Long id);

    FormationResponse update(Long id, FormationRequest request);

    void delete(Long id);
}
