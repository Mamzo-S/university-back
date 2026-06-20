package com.universite.security;

import com.universite.entity.RoleName;
import com.universite.entity.Utilisateur;
import com.universite.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserManagementAuthorization {

    private final UtilisateurRepository utilisateurRepository;

    public void assertCanCreateUsers(String actorEmail, Set<RoleName> requestedRoles) {
        Utilisateur actor = loadActor(actorEmail);

        if (actor.hasRole(RoleName.ADMIN)) {
            return;
        }

        if (actor.hasRole(RoleName.RESPONSABLE_FORMATION)
                && requestedRoles.size() == 1
                && requestedRoles.contains(RoleName.FORMATEUR)) {
            return;
        }

        throw new AccessDeniedException(
                "Vous n'avez pas les droits pour créer ce type de compte"
        );
    }

    public void assertCanManageUser(String actorEmail, Utilisateur target) {
        Utilisateur actor = loadActor(actorEmail);

        if (actor.getId().equals(target.getId())) {
            throw new AccessDeniedException("Vous ne pouvez pas modifier votre propre compte depuis cette interface");
        }

        if (actor.hasRole(RoleName.ADMIN)) {
            return;
        }

        if (actor.hasRole(RoleName.RESPONSABLE_FORMATION) && isFormateurOnly(target)) {
            return;
        }

        throw new AccessDeniedException(
                "Vous n'avez pas les droits pour gérer ce compte"
        );
    }

    private Utilisateur loadActor(String actorEmail) {
        return utilisateurRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new AccessDeniedException("Utilisateur introuvable"));
    }

    private boolean isFormateurOnly(Utilisateur utilisateur) {
        return utilisateur.getRoles().stream().allMatch(role -> role.getNom() == RoleName.FORMATEUR);
    }
}
