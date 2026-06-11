package com.universite.auth;

import com.universite.entity.Role;
import com.universite.entity.RoleName;
import com.universite.entity.Utilisateur;
import com.universite.repository.RoleRepository;
import com.universite.repository.UtilisateurRepository;
import com.universite.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Role role = roleRepository.findByNom(RoleName.ETUDIANT)
                .orElseThrow(() ->
                        new RuntimeException("Role ETUDIANT introuvable")
                );

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(
                        passwordEncoder.encode(request.getMotDePasse())
                )
                .actif(true)
                .dateCreation(LocalDateTime.now())
                .role(role)
                .build();

        utilisateurRepository.save(utilisateur);

        String token = jwtService.generateToken(utilisateur.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole().getNom().toString())
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable")
                );

        boolean passwordMatches = passwordEncoder.matches(
                request.getMotDePasse(),
                utilisateur.getMotDePasse()
        );

        if (!passwordMatches) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        String token = jwtService.generateToken(utilisateur.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole().getNom().toString())
                .build();
    }

    public ApiMessageResponse changePassword(String userEmail, ChangePasswordRequest request) {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), utilisateur.getMotDePasse())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("La confirmation du nouveau mot de passe ne correspond pas");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(utilisateur);

        return new ApiMessageResponse("Mot de passe modifié avec succès");
    }

    public AuthResponse createUser(CreateUserRequest request, String adminEmail) {
        
        // Vérifier que l'utilisateur est ADMIN
        Utilisateur admin = utilisateurRepository
                .findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin introuvable"));

        if (!admin.getRole().getNom().toString().equals("ADMIN")) {
            throw new RuntimeException("Accès refusé: seuls les administrateurs peuvent créer des utilisateurs");
        }

        // Vérifier l'email
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // Déterminer le rôle
        Role role = roleRepository.findByNom(RoleName.valueOf(request.getRole()))
                .orElseThrow(() -> new RuntimeException("Role introuvable: " + request.getRole()));

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .actif(true)
                .dateCreation(LocalDateTime.now())
                .role(role)
                .build();

        utilisateurRepository.save(utilisateur);

        return AuthResponse.builder()
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole().getNom().toString())
                .build();
    }
}
