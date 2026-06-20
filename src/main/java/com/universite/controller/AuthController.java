package com.universite.controller;

import com.universite.auth.*;
import com.universite.dto.MembreResponse;
import com.universite.security.JwtService;
import com.universite.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AdminUserService adminUserService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public AuthResponse register(
            @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse me(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AuthResponse createUser(
            @RequestBody CreateUserRequest request,
            @RequestHeader("Authorization") String bearerToken
    ) {
        // Extraire l'email de l'admin depuis le token
        String token = bearerToken.replace("Bearer ", "");
        String adminEmail = jwtService.extractUsername(token);

        return authService.createUser(request, adminEmail);
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiMessageResponse changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        return authService.changePassword(userEmail, request);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public MembreResponse updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = jwtService.extractUsername(bearerToken.replace("Bearer ", ""));
        return adminUserService.updateUser(id, request, adminEmail);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String bearerToken
    ) {
        String adminEmail = jwtService.extractUsername(bearerToken.replace("Bearer ", ""));
        adminUserService.deleteUser(id, adminEmail);
        return ResponseEntity.noContent().build();
    }
}
