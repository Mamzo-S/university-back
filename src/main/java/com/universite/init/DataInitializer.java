package com.universite.init;

import com.universite.entity.Role;
import com.universite.entity.RoleName;
import com.universite.entity.Utilisateur;
import com.universite.repository.RoleRepository;
import com.universite.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // =========================
        // CREATION DES ROLES
        // =========================

        for (RoleName roleName : RoleName.values()) {

            if (roleRepository.findByNom(roleName).isEmpty()) {

                Role role = Role.builder()
                        .nom(roleName)
                        .build();

                roleRepository.save(role);
            }
        }

        // =========================
        // CREATION ADMIN
        // =========================

        String adminEmail = "admin@universite.sn";

        if (utilisateurRepository.findByEmail(adminEmail).isEmpty()) {

            Role adminRole = roleRepository
                    .findByNom(RoleName.ADMIN)
                    .orElseThrow();

            Utilisateur admin = Utilisateur.builder()
                    .nom("Admin")
                    .prenom("Université")
                    .email(adminEmail)
                    .motDePasse(
                            passwordEncoder.encode("admin123")
                    )
                    .actif(true)
                    .dateCreation(LocalDateTime.now())
                    .role(adminRole)
                    .build();

            utilisateurRepository.save(admin);

            System.out.println("ADMIN CREE AVEC SUCCES");
        }
    }
}
