package com.universite.service.impl;

import com.universite.auth.UpdateUserRequest;
import com.universite.dto.MembreResponse;
import com.universite.entity.*;
import com.universite.mapper.EtudiantMapper;
import com.universite.mapper.MembreMapper;
import com.universite.repository.*;
import com.universite.security.UserManagementAuthorization;
import com.universite.service.AdminUserService;
import com.universite.util.NiveauEtudeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@universite.sn";

    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final FormateurRepository formateurRepository;
    private final PersonnelRepository personnelRepository;
    private final PromotionRepository promotionRepository;
    private final NoteRepository noteRepository;
    private final SeanceRepository seanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserManagementAuthorization userManagementAuthorization;

    @Override
    @Transactional
    public MembreResponse updateUser(Long utilisateurId, UpdateUserRequest request, String actorEmail) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        userManagementAuthorization.assertCanManageUser(actorEmail, utilisateur);

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim();
            utilisateurRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(utilisateurId)) {
                    throw new RuntimeException("Email déjà utilisé");
                }
            });
            utilisateur.setEmail(email);
        }

        if (request.getNom() != null) {
            utilisateur.setNom(request.getNom().trim());
        }
        if (request.getPrenom() != null) {
            utilisateur.setPrenom(request.getPrenom().trim());
        }
        if (request.getTelephone() != null) {
            utilisateur.setTelephone(request.getTelephone().trim());
        }
        if (request.getActif() != null) {
            utilisateur.setActif(request.getActif());
        }

        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            if (request.getMotDePasse().length() < 6) {
                throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
            }
            utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        }

        utilisateurRepository.save(utilisateur);
        updateProfiles(utilisateur, request);

        return toMembreResponse(utilisateur);
    }

    @Override
    @Transactional
    public void deleteUser(Long utilisateurId, String actorEmail) {
        Utilisateur actor = utilisateurRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (actor.getId().equals(utilisateurId)) {
            throw new RuntimeException("Vous ne pouvez pas supprimer votre propre compte");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        userManagementAuthorization.assertCanManageUser(actorEmail, utilisateur);

        if (DEFAULT_ADMIN_EMAIL.equalsIgnoreCase(utilisateur.getEmail())) {
            throw new RuntimeException("Le compte administrateur par défaut ne peut pas être supprimé");
        }

        if (utilisateur.hasRole(RoleName.ADMIN)) {
            long adminCount = utilisateurRepository.findByRoleNom(RoleName.ADMIN).size();
            if (adminCount <= 1) {
                throw new RuntimeException("Impossible de supprimer le dernier administrateur");
            }
        }

        etudiantRepository.findByUtilisateur_Id(utilisateurId).ifPresent(etudiant -> {
            if (!noteRepository.findByEtudiantId(etudiant.getId()).isEmpty()) {
                throw new RuntimeException(
                        "Impossible de supprimer un étudiant ayant des notes. Désactivez le compte à la place."
                );
            }
            etudiantRepository.delete(etudiant);
        });

        formateurRepository.findByUtilisateur_Id(utilisateurId).ifPresent(formateur -> {
            if (!seanceRepository.findByFormateurId(formateur.getId()).isEmpty()) {
                throw new RuntimeException(
                        "Impossible de supprimer un enseignant assigné à des séances. Désactivez le compte à la place."
                );
            }
            formateurRepository.delete(formateur);
        });
        personnelRepository.findByUtilisateur_Id(utilisateurId)
                .ifPresent(personnelRepository::delete);

        utilisateur.getRoles().clear();
        utilisateurRepository.save(utilisateur);
        utilisateurRepository.delete(utilisateur);
    }

    private void updateProfiles(Utilisateur utilisateur, UpdateUserRequest request) {
        if (utilisateur.hasRole(RoleName.ETUDIANT)) {
            updateEtudiantProfile(utilisateur, request);
        }
        if (utilisateur.hasRole(RoleName.FORMATEUR)) {
            updateFormateurProfile(utilisateur, request);
        }
        if (utilisateur.getRoles().stream().anyMatch(role -> isPersonnelRole(role.getNom()))) {
            updatePersonnelProfile(utilisateur, request);
        }
    }

    private void updateEtudiantProfile(Utilisateur utilisateur, UpdateUserRequest request) {
        Etudiant etudiant = etudiantRepository.findByUtilisateur_Id(utilisateur.getId())
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        if (request.getIne() != null && !request.getIne().isBlank()) {
            String ine = request.getIne().trim();
            etudiantRepository.findByIne(ine).ifPresent(existing -> {
                if (!existing.getId().equals(etudiant.getId())) {
                    throw new RuntimeException("INE déjà utilisé");
                }
            });
            etudiant.setIne(ine);
        }

        if (request.getDateNaissance() != null && !request.getDateNaissance().isBlank()) {
            etudiant.setDateNaissance(parseDateNaissance(request.getDateNaissance()));
        }

        if (request.getNiveau() != null && !request.getNiveau().isBlank()) {
            etudiant.setNiveau(NiveauEtudeParser.parseRequired(request.getNiveau()));
        }

        if (request.getPromotionId() != null
                || (request.getPromotionNom() != null && !request.getPromotionNom().isBlank())) {
            etudiant.setPromotion(resolvePromotion(request));
        }

        etudiantRepository.save(etudiant);
    }

    private void updateFormateurProfile(Utilisateur utilisateur, UpdateUserRequest request) {
        Formateur formateur = formateurRepository.findByUtilisateur_Id(utilisateur.getId())
                .orElseThrow(() -> new RuntimeException("Profil formateur introuvable"));

        if (request.getGrade() != null && !request.getGrade().isBlank()) {
            formateur.setGrade(request.getGrade().trim());
        }
        if (request.getSpecialite() != null) {
            formateur.setSpecialite(request.getSpecialite().trim());
        }

        formateurRepository.save(formateur);
    }

    private void updatePersonnelProfile(Utilisateur utilisateur, UpdateUserRequest request) {
        Personnel personnel = personnelRepository.findByUtilisateur_Id(utilisateur.getId())
                .orElseThrow(() -> new RuntimeException("Profil personnel introuvable"));

        if (request.getFonction() != null && !request.getFonction().isBlank()) {
            personnel.setFonction(request.getFonction().trim());
        }
        if (request.getService() != null) {
            personnel.setService(request.getService().trim());
        }

        personnelRepository.save(personnel);
    }

    private Promotion resolvePromotion(UpdateUserRequest request) {
        if (request.getPromotionId() != null) {
            return promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new RuntimeException("Promotion introuvable"));
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

    private LocalDate parseDateNaissance(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Date de naissance invalide (format attendu : AAAA-MM-JJ)");
        }
    }

    private boolean isPersonnelRole(RoleName roleName) {
        return roleName == RoleName.PERSONNEL_ADMIN
                || roleName == RoleName.TUTEUR
                || roleName == RoleName.RESPONSABLE_FORMATION
                || roleName == RoleName.SERVICE_INSERTION;
    }

    private MembreResponse toMembreResponse(Utilisateur utilisateur) {
        return etudiantRepository.findByUtilisateur_Id(utilisateur.getId())
                .map(EtudiantMapper::toDTO)
                .map(MembreMapper::fromEtudiant)
                .or(() -> formateurRepository.findByUtilisateur_Id(utilisateur.getId())
                        .map(MembreMapper::fromFormateur))
                .or(() -> personnelRepository.findByUtilisateur_Id(utilisateur.getId())
                        .map(MembreMapper::fromPersonnel))
                .orElseGet(() -> {
                    RoleName primaryRole = utilisateur.getRoles().stream()
                            .map(Role::getNom)
                            .min(Comparator.comparing(Enum::name))
                            .orElse(RoleName.ADMIN);
                    return MembreMapper.fromUtilisateur(utilisateur, primaryRole);
                });
    }
}
