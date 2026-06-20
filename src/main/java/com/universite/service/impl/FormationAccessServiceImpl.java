package com.universite.service.impl;

import com.universite.dto.parcours.FormationParcoursDto;
import com.universite.entity.Etudiant;
import com.universite.entity.Formateur;
import com.universite.entity.Formation;
import com.universite.entity.RoleName;
import com.universite.entity.Utilisateur;
import com.universite.mapper.FormationParcoursMapper;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.FormateurRepository;
import com.universite.repository.FormationRepository;
import com.universite.repository.UtilisateurRepository;
import com.universite.service.FormationAccessService;
import com.universite.util.EtudiantProfileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormationAccessServiceImpl implements FormationAccessService {

    private final EtudiantRepository etudiantRepository;
    private final FormationRepository formationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FormateurRepository formateurRepository;
    private final FormationParcoursMapper formationParcoursMapper;

    @Override
    public void assertCanReadFormation(Formation formation, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return;
        }

        Etudiant etudiant = etudiantRepository.findByUtilisateur_EmailWithProfile(userEmail).orElse(null);
        if (etudiant == null) {
            return;
        }

        boolean allowed = EtudiantProfileUtils.findAccessibleModules(etudiant, formationRepository).stream()
                .anyMatch(item -> item.getId().equals(formation.getId()));
        if (!allowed) {
            throw new AccessDeniedException("Accès refusé à ce module");
        }
    }

    @Override
    public void assertCanManageFormation(Formation formation, String userEmail) {
        if (canManageAllFormations(userEmail)) {
            return;
        }
        assertFormateurCanManage(formation, userEmail);
    }

    @Override
    public void assertCanDeleteFormation(Formation formation, String userEmail) {
        if (canManageAllFormations(userEmail)) {
            return;
        }
        assertFormateurIsCreator(formation, userEmail);
    }

    @Override
    public void assertCanUpdateParcours(Formation formation, String userEmail) {
        if (canManageAllFormations(userEmail)) {
            return;
        }
        assertFormateurCanManage(formation, userEmail);
    }

    private boolean canManageAllFormations(String userEmail) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("Utilisateur introuvable"));

        return utilisateur.hasRole(RoleName.ADMIN)
                || utilisateur.hasRole(RoleName.RESPONSABLE_FORMATION);
    }

    private void assertFormateurCanManage(Formation formation, String userEmail) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("Utilisateur introuvable"));

        if (!utilisateur.hasRole(RoleName.FORMATEUR)) {
            throw new AccessDeniedException("Accès refusé à cette formation");
        }

        if (isAssignedToFormateur(formation, userEmail) || isCreatedByFormateur(formation, userEmail)) {
            return;
        }

        throw new AccessDeniedException("Accès refusé à cette formation");
    }

    private void assertFormateurIsCreator(Formation formation, String userEmail) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("Utilisateur introuvable"));

        if (!utilisateur.hasRole(RoleName.FORMATEUR)) {
            throw new AccessDeniedException("Accès refusé à cette formation");
        }

        if (isCreatedByFormateur(formation, userEmail)) {
            return;
        }

        throw new AccessDeniedException("Seul le formateur créateur peut supprimer ce module");
    }

    private boolean isAssignedToFormateur(Formation formation, String userEmail) {
        Set<Long> assignedIds = loadAssignedFormationIds(userEmail);
        return formation.getId() != null && assignedIds.contains(formation.getId());
    }

    private boolean isCreatedByFormateur(Formation formation, String userEmail) {
        Formateur formateur = formateurRepository.findWithFormationsByUtilisateur_Email(userEmail)
                .orElse(null);
        if (formateur == null) {
            return false;
        }

        String trainerName = resolveTrainerName(formateur);
        if (trainerName.isBlank()) {
            return false;
        }

        FormationParcoursDto parcours = formationParcoursMapper.toDto(formation);
        return trainerName.equalsIgnoreCase(
                parcours.getTrainerName() != null ? parcours.getTrainerName().trim() : ""
        );
    }

    private Set<Long> loadAssignedFormationIds(String userEmail) {
        Formateur formateur = formateurRepository.findWithFormationsByUtilisateur_Email(userEmail)
                .orElse(null);
        if (formateur == null || formateur.getFormations() == null) {
            return Set.of();
        }

        return formateur.getFormations().stream()
                .map(Formation::getId)
                .collect(Collectors.toSet());
    }

    private String resolveTrainerName(Formateur formateur) {
        Utilisateur utilisateur = formateur.getUtilisateur();
        if (utilisateur == null) {
            return "";
        }
        return (utilisateur.getPrenom() + " " + utilisateur.getNom()).trim();
    }
}
