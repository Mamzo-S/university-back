package com.universite.service;

import com.universite.dto.GroupeEtudiantRequest;
import com.universite.dto.GroupeEtudiantResponse;

import java.util.List;

public interface GroupeEtudiantService {

    List<GroupeEtudiantResponse> listAll(Long promotionId, Long formationId, Long filiereId);

    GroupeEtudiantResponse getById(Long id);

    GroupeEtudiantResponse create(GroupeEtudiantRequest request);

    GroupeEtudiantResponse update(Long id, GroupeEtudiantRequest request);

    void delete(Long id);
}
