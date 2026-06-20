package com.universite.service;

import com.universite.dto.PartenaireRequest;
import com.universite.dto.PartenaireResponse;

import java.util.List;

public interface PartenaireService {

    List<PartenaireResponse> getAll();

    PartenaireResponse getById(Long id);

    PartenaireResponse create(PartenaireRequest request);

    PartenaireResponse update(Long id, PartenaireRequest request);

    void delete(Long id);
}
