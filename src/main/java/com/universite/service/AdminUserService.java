package com.universite.service;

import com.universite.auth.UpdateUserRequest;
import com.universite.dto.MembreResponse;

public interface AdminUserService {

    MembreResponse updateUser(Long utilisateurId, UpdateUserRequest request, String adminEmail);

    void deleteUser(Long utilisateurId, String adminEmail);
}
