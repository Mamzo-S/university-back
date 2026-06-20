package com.universite.auth;

import com.universite.entity.*;
import com.universite.repository.*;
import com.universite.security.JwtService;
import com.universite.security.UserManagementAuthorization;
import com.universite.util.NiveauEtudeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final EtudiantRepository etudiantRepository;
    private final FormateurRepository formateurRepository;
    private final PersonnelRepository personnelRepository;
    private final PromotionRepository promotionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserManagementAuthorization userManagementAuthorization;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Role etudiantRole = roleRepository.findByNom(RoleName.ETUDIANT)
                .orElseThrow(() -> new RuntimeException("Rôle ETUDIANT introuvable"));

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .actif(true)
                .roles(new HashSet<>())
                .build();

        utilisateur.addRole(etudiantRole);
        utilisateurRepository.save(utilisateur);

        String token = jwtService.generateToken(utilisateur.getEmail());
        return buildAuthResponse(utilisateur, token);
    }

    public AuthResponse login(LoginRequest request) {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        String token = jwtService.generateToken(utilisateur.getEmail());
        return buildAuthResponse(utilisateur, token);
    }

    public UserResponse getCurrentUser(String userEmail) {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return toUserResponse(utilisateur);
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

    @Transactional
    public AuthResponse createUser(CreateUserRequest request, String actorEmail) {
        Set<RoleName> roleNames = resolveRoleNames(request);
        userManagementAuthorization.assertCanCreateUsers(actorEmail, roleNames);

        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Set<Role> roles = new HashSet<>();

        for (RoleName roleName : roleNames) {
            Role role = roleRepository.findByNom(roleName)
                    .orElseThrow(() -> new RuntimeException("Rôle introuvable: " + roleName));
            roles.add(role);
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .actif(true)
                .roles(new HashSet<>())
                .build();

        for (Role role : roles) {
            utilisateur.addRole(role);
        }

        utilisateurRepository.save(utilisateur);
        createProfilesForRoles(utilisateur, roleNames, request);

        return buildAuthResponse(utilisateur, null);
    }

    private void createProfilesForRoles(
            Utilisateur utilisateur,
            Set<RoleName> roleNames,
            CreateUserRequest request
    ) {
        if (roleNames.contains(RoleName.ETUDIANT)) {
            createEtudiantProfile(utilisateur, request);
        }
        if (roleNames.contains(RoleName.FORMATEUR)) {
            createFormateurProfile(utilisateur, request);
        }
        for (RoleName roleName : roleNames) {
            if (isPersonnelRole(roleName)) {
                createPersonnelProfile(utilisateur, roleName, request);
            }
        }
    }

    private void createEtudiantProfile(Utilisateur utilisateur, CreateUserRequest request) {
        if (request.getIne() == null || request.getIne().isBlank()) {
            throw new RuntimeException("INE obligatoire pour créer un étudiant");
        }

        if (etudiantRepository.findByIne(request.getIne()).isPresent()) {
            throw new RuntimeException("INE déjà utilisé");
        }

        Etudiant etudiant = Etudiant.builder()
                .ine(request.getIne().trim())
                .dateNaissance(resolveDateNaissance(request))
                .niveau(resolveNiveau(request.getNiveau()))
                .utilisateur(utilisateur)
                .promotion(resolvePromotion(request))
                .build();

        etudiantRepository.save(etudiant);
    }

    private LocalDate resolveDateNaissance(CreateUserRequest request) {
        if (request.getDateNaissance() != null && !request.getDateNaissance().isBlank()) {
            try {
                return LocalDate.parse(request.getDateNaissance());
            } catch (DateTimeParseException ex) {
                throw new RuntimeException("Date de naissance invalide (format attendu : AAAA-MM-JJ)");
            }
        }

        return LocalDate.of(2000, 1, 1);
    }

    private NiveauEtude resolveNiveau(String niveau) {
        NiveauEtude parsed = NiveauEtudeParser.parse(niveau);
        return parsed != null ? parsed : NiveauEtude.LICENCE_1;
    }

    private Promotion resolvePromotion(CreateUserRequest request) {
        if (request.getPromotionId() != null) {
            return promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new RuntimeException("Promotion introuvable"));
        }

        if (request.getPromotionNom() == null || request.getPromotionNom().isBlank()) {
            return null;
        }

        String promotionNom = request.getPromotionNom().trim();

        return promotionRepository.findByNomIgnoreCase(promotionNom)
                .orElseGet(() -> promotionRepository.save(
                        Promotion.builder()
                                .nom(promotionNom)
                                .anneeAcademique(promotionNom)
                                .build()
                ));
    }

    private void createFormateurProfile(Utilisateur utilisateur, CreateUserRequest request) {
        if (formateurRepository.findByUtilisateur_Id(utilisateur.getId()).isPresent()) {
            return;
        }

        Formateur formateur = Formateur.builder()
                .grade(request.getGrade() != null ? request.getGrade() : "Formateur")
                .specialite(request.getSpecialite())
                .utilisateur(utilisateur)
                .build();

        formateurRepository.save(formateur);
    }

    private void createPersonnelProfile(
            Utilisateur utilisateur,
            RoleName roleName,
            CreateUserRequest request
    ) {
        if (personnelRepository.findByUtilisateur_Id(utilisateur.getId()).isPresent()) {
            return;
        }

        TypePersonnel type = resolveTypePersonnel(roleName, request);

        String fonction = request.getFonction();
        if (fonction == null || fonction.isBlank()) {
            fonction = type.name().replace('_', ' ');
        }

        Personnel personnel = Personnel.builder()
                .fonction(fonction)
                .service(request.getService())
                .type(type)
                .utilisateur(utilisateur)
                .build();

        personnelRepository.save(personnel);
    }

    private TypePersonnel resolveTypePersonnel(RoleName roleName, CreateUserRequest request) {
        if (request.getTypePersonnel() != null && !request.getTypePersonnel().isBlank()) {
            return TypePersonnel.valueOf(request.getTypePersonnel());
        }

        return switch (roleName) {
            case PERSONNEL_ADMIN -> TypePersonnel.PERSONNEL_ADMIN;
            case TUTEUR -> TypePersonnel.TUTEUR;
            case RESPONSABLE_FORMATION -> TypePersonnel.RESPONSABLE_FORMATION;
            case SERVICE_INSERTION -> TypePersonnel.SERVICE_INSERTION;
            default -> throw new RuntimeException("Rôle personnel invalide: " + roleName);
        };
    }

    private boolean isPersonnelRole(RoleName roleName) {
        return roleName == RoleName.PERSONNEL_ADMIN
                || roleName == RoleName.TUTEUR
                || roleName == RoleName.RESPONSABLE_FORMATION
                || roleName == RoleName.SERVICE_INSERTION;
    }

    private Set<RoleName> resolveRoleNames(CreateUserRequest request) {
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            return request.getRoles().stream()
                    .map(this::normalizeRoleName)
                    .map(RoleName::valueOf)
                    .collect(Collectors.toSet());
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            return Set.of(RoleName.valueOf(normalizeRoleName(request.getRole())));
        }

        throw new RuntimeException("Au moins un rôle est requis");
    }

    private String normalizeRoleName(String role) {
        if ("PROFESSEUR".equalsIgnoreCase(role)) {
            return RoleName.FORMATEUR.name();
        }
        return role.toUpperCase();
    }

    private AuthResponse buildAuthResponse(Utilisateur utilisateur, String token) {
        UserResponse user = toUserResponse(utilisateur);
        String primaryRole = user.getRoles().isEmpty() ? null : user.getRoles().get(0);

        return AuthResponse.builder()
                .token(token)
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(primaryRole)
                .user(user)
                .build();
    }

    private UserResponse toUserResponse(Utilisateur utilisateur) {
        List<String> roleNames = utilisateur.getRoles().stream()
                .map(role -> role.getNom().name())
                .sorted()
                .toList();

        return UserResponse.builder()
                .id(String.valueOf(utilisateur.getId()))
                .email(utilisateur.getEmail())
                .firstName(utilisateur.getPrenom())
                .lastName(utilisateur.getNom())
                .roles(roleNames)
                .build();
    }
}
