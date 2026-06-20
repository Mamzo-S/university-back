package com.universite.service;

import com.universite.dto.AnneeAcademiqueRequest;
import com.universite.dto.AnneeAcademiqueResponse;

import java.util.List;

public interface AnneeAcademiqueService {

    List<AnneeAcademiqueResponse> getAll();

    AnneeAcademiqueResponse getById(Long id);

    AnneeAcademiqueResponse create(AnneeAcademiqueRequest request);

    AnneeAcademiqueResponse update(Long id, AnneeAcademiqueRequest request);

    void delete(Long id);
}
