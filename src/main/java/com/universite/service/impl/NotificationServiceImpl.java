package com.universite.service.impl;

import com.universite.dto.NotificationBroadcastRequest;
import com.universite.dto.NotificationResponse;
import com.universite.dto.NotificationUnreadCountResponse;
import com.universite.entity.*;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.FiliereRepository;
import com.universite.repository.NotificationRepository;
import com.universite.repository.UtilisateurRepository;
import com.universite.service.NotificationService;
import com.universite.util.EtudiantProfileUtils;
import com.universite.util.NiveauEtudeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EtudiantRepository etudiantRepository;
    private final FiliereRepository filiereRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> listForCurrentUser(String userEmail) {
        Utilisateur utilisateur = loadUser(userEmail);
        return notificationRepository.findByDestinataireIdOrderByDateCreationDesc(utilisateur.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationUnreadCountResponse countUnreadForCurrentUser(String userEmail) {
        Utilisateur utilisateur = loadUser(userEmail);
        return NotificationUnreadCountResponse.builder()
                .count(notificationRepository.countByDestinataireIdAndLuFalse(utilisateur.getId()))
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String userEmail) {
        Utilisateur utilisateur = loadUser(userEmail);
        Notification notification = notificationRepository.findByIdAndDestinataireId(
                        notificationId,
                        utilisateur.getId()
                )
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));

        if (!Boolean.TRUE.equals(notification.getLu())) {
            notification.setLu(true);
            notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userEmail) {
        Utilisateur utilisateur = loadUser(userEmail);
        notificationRepository.markAllAsRead(utilisateur.getId());
    }

    @Override
    @Transactional
    public int broadcastToStudents(NotificationBroadcastRequest request) {
        validateBroadcastRequest(request);

        List<Etudiant> targets = resolveBroadcastTargets(request);
        if (targets.isEmpty()) {
            return 0;
        }

        Set<Long> notifiedUserIds = new HashSet<>();
        for (Etudiant etudiant : targets) {
            Utilisateur utilisateur = etudiant.getUtilisateur();
            if (utilisateur == null || !notifiedUserIds.add(utilisateur.getId())) {
                continue;
            }
            createNotification(
                    utilisateur,
                    request.getTitre().trim(),
                    request.getMessage().trim(),
                    NotificationType.ANNONCE,
                    request.getLien(),
                    null
            );
        }

        return notifiedUserIds.size();
    }

    @Override
    @Transactional
    public void notifySeanceCreated(Seance seance) {
        notifySeanceChange(seance, "ajoutée", null);
    }

    @Override
    @Transactional
    public void notifySeanceUpdated(Seance seance, Seance previousState) {
        notifySeanceChange(seance, "modifiée", previousState);
    }

    @Override
    @Transactional
    public void notifySeanceDeleted(Seance seance) {
        notifySeanceChange(seance, "supprimée", null);
    }

    @Override
    @Transactional
    public void notifyFormateurModulesAssigned(Formateur formateur, int moduleCount) {
        Utilisateur utilisateur = formateur.getUtilisateur();
        if (utilisateur == null) {
            return;
        }

        String message = moduleCount == 0
                ? "Vos modules assignés ont été mis à jour : aucun module ne vous est actuellement attribué."
                : "Vos modules assignés ont été mis à jour : "
                        + moduleCount
                        + " module(s) vous sont désormais attribués.";

        createNotification(
                utilisateur,
                "Modules mis à jour",
                message,
                NotificationType.MODULE_ASSIGNE,
                "/trainer/formations",
                formateur.getId()
        );
    }

    @Override
    @Transactional
    public void notifyBulletinPublished(Etudiant etudiant, String semestre, String anneeAcademique) {
        Utilisateur utilisateur = etudiant.getUtilisateur();
        if (utilisateur == null) {
            return;
        }

        createNotification(
                utilisateur,
                "Bulletin publié",
                "Votre bulletin "
                        + semestre
                        + " — "
                        + anneeAcademique
                        + " est disponible.",
                NotificationType.BULLETIN,
                "/student/bulletins",
                etudiant.getId()
        );
    }

    private void notifySeanceChange(Seance seance, String action, Seance previousState) {
        String coursLabel = resolveCoursLabel(seance);
        String horaire = formatPlageHoraire(seance);
        String jour = formatJour(seance.getJourSemaine());
        String message = "La séance « "
                + coursLabel
                + " » du "
                + jour
                + " ("
                + horaire
                + ") a été "
                + action
                + ".";

        Formateur formateur = seance.getFormateur();
        if (formateur != null && formateur.getUtilisateur() != null) {
            createNotification(
                    formateur.getUtilisateur(),
                    "Emploi du temps " + action,
                    message,
                    NotificationType.EMPLOI_DU_TEMPS,
                    "/trainer/emploi-du-temps",
                    seance.getId()
            );
        }

        if (previousState != null
                && previousState.getFormateur() != null
                && previousState.getFormateur().getUtilisateur() != null
                && (formateur == null
                        || !previousState.getFormateur().getId().equals(formateur.getId()))) {
            createNotification(
                    previousState.getFormateur().getUtilisateur(),
                    "Emploi du temps modifié",
                    "Vous avez été retiré de la séance « "
                            + resolveCoursLabel(previousState)
                            + " » du "
                            + formatJour(previousState.getJourSemaine())
                            + ".",
                    NotificationType.EMPLOI_DU_TEMPS,
                    "/trainer/emploi-du-temps",
                    seance.getId()
            );
        }

        notifyAffectedStudents(seance, action, message);
    }

    private void notifyAffectedStudents(Seance seance, String action, String message) {
        Formation formation = seance.getCours() != null ? seance.getCours().getFormation() : null;
        if (formation == null || formation.getFiliere() == null) {
            return;
        }

        Long filiereId = formation.getFiliere().getId();
        List<Etudiant> candidates = etudiantRepository.findByFiliereId(filiereId);

        Set<Long> notifiedUserIds = new HashSet<>();
        for (Etudiant etudiant : candidates) {
            if (!isSeanceVisibleToStudent(seance, etudiant)) {
                continue;
            }
            Utilisateur utilisateur = etudiant.getUtilisateur();
            if (utilisateur == null || !notifiedUserIds.add(utilisateur.getId())) {
                continue;
            }
            createNotification(
                    utilisateur,
                    "Emploi du temps " + action,
                    message,
                    NotificationType.EMPLOI_DU_TEMPS,
                    "/student/emploi-du-temps",
                    seance.getId()
            );
        }
    }

    private boolean isSeanceVisibleToStudent(Seance seance, Etudiant etudiant) {
        Formation formation = seance.getCours().getFormation();
        Filiere filiere = EtudiantProfileUtils.getStudentFiliere(etudiant);
        if (filiere == null || formation.getFiliere() == null) {
            return false;
        }
        if (!filiere.getId().equals(formation.getFiliere().getId())) {
            return false;
        }
        if (formation.getNiveau() != null && formation.getNiveau().equals(etudiant.getNiveau())) {
            return true;
        }
        if (etudiant.getPromotion() != null && etudiant.getPromotion().getFormation() != null) {
            Formation promotionModule = etudiant.getPromotion().getFormation();
            return promotionModule.getId().equals(formation.getId())
                    && EtudiantProfileUtils.belongsToFiliere(promotionModule, filiere);
        }
        return false;
    }

    private List<Etudiant> resolveBroadcastTargets(NotificationBroadcastRequest request) {
        return switch (request.getPortee()) {
            case FILIERE -> etudiantRepository.findByFiliereId(request.getFiliereId());
            case NIVEAU -> {
                NiveauEtude niveau = NiveauEtudeParser.parseRequired(request.getNiveau());
                yield etudiantRepository.findByNiveau(niveau);
            }
            case FILIERE_ET_NIVEAU -> {
                NiveauEtude niveau = NiveauEtudeParser.parseRequired(request.getNiveau());
                yield etudiantRepository.findByFiliereIdAndNiveau(
                        request.getFiliereId(),
                        niveau
                );
            }
        };
    }

    private void validateBroadcastRequest(NotificationBroadcastRequest request) {
        if (request.getTitre() == null || request.getTitre().isBlank()) {
            throw new RuntimeException("Le titre est obligatoire");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new RuntimeException("Le message est obligatoire");
        }
        if (request.getPortee() == null) {
            throw new RuntimeException("Le périmètre de diffusion est obligatoire");
        }

        switch (request.getPortee()) {
            case FILIERE -> {
                if (request.getFiliereId() == null) {
                    throw new RuntimeException("La filière est obligatoire pour ce périmètre");
                }
                filiereRepository.findById(request.getFiliereId())
                        .orElseThrow(() -> new RuntimeException("Filière introuvable"));
            }
            case NIVEAU -> {
                if (request.getNiveau() == null || request.getNiveau().isBlank()) {
                    throw new RuntimeException("Le niveau est obligatoire pour ce périmètre");
                }
                NiveauEtudeParser.parseRequired(request.getNiveau());
            }
            case FILIERE_ET_NIVEAU -> {
                if (request.getFiliereId() == null) {
                    throw new RuntimeException("La filière est obligatoire pour ce périmètre");
                }
                if (request.getNiveau() == null || request.getNiveau().isBlank()) {
                    throw new RuntimeException("Le niveau est obligatoire pour ce périmètre");
                }
                filiereRepository.findById(request.getFiliereId())
                        .orElseThrow(() -> new RuntimeException("Filière introuvable"));
                NiveauEtudeParser.parseRequired(request.getNiveau());
            }
        }
    }

    private void createNotification(
            Utilisateur destinataire,
            String titre,
            String message,
            NotificationType type,
            String lien,
            Long referenceId
    ) {
        notificationRepository.save(Notification.builder()
                .destinataire(destinataire)
                .titre(titre)
                .message(message)
                .type(type)
                .lu(false)
                .dateCreation(LocalDateTime.now())
                .lien(lien)
                .referenceId(referenceId)
                .build());
    }

    private Utilisateur loadUser(String userEmail) {
        return utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .titre(notification.getTitre())
                .message(notification.getMessage())
                .type(notification.getType())
                .lu(notification.getLu())
                .dateCreation(notification.getDateCreation())
                .lien(notification.getLien())
                .referenceId(notification.getReferenceId())
                .build();
    }

    private String resolveCoursLabel(Seance seance) {
        if (seance.getCours() == null) {
            return "cours inconnu";
        }
        if (seance.getCours().getNom() != null && !seance.getCours().getNom().isBlank()) {
            return seance.getCours().getNom();
        }
        return "cours #" + seance.getCours().getId();
    }

    private String formatJour(JourSemaine jour) {
        return switch (jour) {
            case LUNDI -> "lundi";
            case MARDI -> "mardi";
            case MERCREDI -> "mercredi";
            case JEUDI -> "jeudi";
            case VENDREDI -> "vendredi";
            case SAMEDI -> "samedi";
            case DIMANCHE -> "dimanche";
        };
    }

    private String formatPlageHoraire(Seance seance) {
        return seance.getHeureDebut().toString().substring(0, 5)
                + "–"
                + seance.getHeureFin().toString().substring(0, 5);
    }
}
