package com.universite.controller;

import com.universite.auth.*;
import com.universite.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
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

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
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
}
