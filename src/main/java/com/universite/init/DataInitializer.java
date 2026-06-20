package com.universite.init;

import com.universite.entity.Role;
import com.universite.entity.RoleName;
import com.universite.entity.Utilisateur;
import com.universite.repository.RoleRepository;
import com.universite.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@universite.sn";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private static final Map<RoleName, String> ROLE_DESCRIPTIONS = Map.of(
            RoleName.ADMIN, "Administrateur système",
            RoleName.FORMATEUR, "Enseignant / formateur",
            RoleName.ETUDIANT, "Étudiant",
            RoleName.PERSONNEL_ADMIN, "Personnel administratif",
            RoleName.TUTEUR, "Tuteur pédagogique",
            RoleName.RESPONSABLE_FORMATION, "Responsable de formation",
            RoleName.SERVICE_INSERTION, "Service insertion professionnelle"
    );

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        ensureDefaultAdmin();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByNom(roleName).ifPresentOrElse(
                    existing -> {
                        String description = ROLE_DESCRIPTIONS.get(roleName);
                        if (existing.getDescription() == null && description != null) {
                            existing.setDescription(description);
                            roleRepository.save(existing);
                        }
                    },
                    () -> {
                        Role role = Role.builder()
                                .nom(roleName)
                                .description(ROLE_DESCRIPTIONS.get(roleName))
                                .build();
                        roleRepository.save(role);
                        log.info("Rôle créé : {}", roleName);
                    }
            );
        }
    }

    private void ensureDefaultAdmin() {
        Role adminRole = roleRepository
                .findByNom(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Rôle ADMIN introuvable après seed"));

        utilisateurRepository.findByEmail(DEFAULT_ADMIN_EMAIL).ifPresentOrElse(
                existing -> linkAdminRoleIfMissing(existing, adminRole),
                () -> createDefaultAdmin(adminRole)
        );
    }

    private void linkAdminRoleIfMissing(Utilisateur admin, Role adminRole) {
        if (admin.hasRole(RoleName.ADMIN)) {
            return;
        }

        admin.addRole(adminRole);
        utilisateurRepository.save(admin);
        log.info("Rôle ADMIN rattaché au compte existant {}", DEFAULT_ADMIN_EMAIL);
    }

    private void createDefaultAdmin(Role adminRole) {
        Utilisateur admin = Utilisateur.builder()
                .nom("Admin")
                .prenom("Université")
                .email(DEFAULT_ADMIN_EMAIL)
                .motDePasse(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .actif(true)
                .roles(new HashSet<>())
                .build();

        admin.addRole(adminRole);
        utilisateurRepository.save(admin);

        log.info("Compte admin par défaut créé : {} / {}", DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
    }
}
