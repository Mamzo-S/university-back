package com.universite.service;

import com.universite.dto.CoursRequest;
import com.universite.dto.CoursResponse;

import java.util.List;

public interface CoursService {
    CoursResponse create(CoursRequest request);
    CoursResponse update(Long id, CoursRequest request);
    CoursResponse getById(Long id);
    List<CoursResponse> getAll();
    List<CoursResponse> getByFormation(Long formationId);
    void delete(Long id);
}
