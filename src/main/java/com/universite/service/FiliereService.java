package com.universite.service;

import com.universite.dto.FiliereDetailResponse;
import com.universite.dto.FiliereRequest;
import com.universite.dto.FiliereResponse;

import java.util.List;

public interface FiliereService {

    FiliereResponse create(FiliereRequest request);

    List<FiliereResponse> getAll();

    FiliereResponse getById(Long id);

    FiliereDetailResponse getDetail(Long id);

    FiliereResponse update(Long id, FiliereRequest request);

    void delete(Long id);
}
