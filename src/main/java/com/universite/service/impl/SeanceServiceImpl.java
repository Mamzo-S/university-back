package com.universite.service.impl;

import com.universite.dto.SeanceRequest;
import com.universite.dto.SeanceResponse;
import com.universite.entity.*;
import com.universite.mapper.SeanceMapper;
import com.universite.repository.*;
import com.universite.service.EmploiDuTempsService;
import com.universite.service.NotificationService;
import com.universite.service.SeanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeanceServiceImpl implements SeanceService {

    private final SeanceRepository seanceRepository;
    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final CoursRepository coursRepository;
    private final FormationRepository formationRepository;
    private final FormateurRepository formateurRepository;
    private final PromotionRepository promotionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmploiDuTempsService emploiDuTempsService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<SeanceResponse> listByPromotion(Long promotionId) {
        return seanceRepository.findByEmploiDuTemps_Promotion_Id(promotionId).stream()
                .map(SeanceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeanceResponse> listAll() {
        return seanceRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((Seance s) -> s.getJourSemaine().getIndex())
                        .thenComparing(Seance::getHeureDebut)
                        .thenComparing(s -> s.getCours().getNom(), Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(SeanceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeanceResponse> listByFormation(Long formationId) {
        return seanceRepository.findByEmploiDuTemps_Promotion_Formation_Id(formationId).stream()
                .map(SeanceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeanceResponse> listForCurrentUser(String userEmail) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (utilisateur.hasRole(RoleName.ETUDIANT)) {
            return emploiDuTempsService.getForCurrentEtudiant(userEmail);
        }

        if (utilisateur.hasRole(RoleName.FORMATEUR)) {
            Formateur formateur = formateurRepository.findByUtilisateur_Id(utilisateur.getId())
                    .orElseThrow(() -> new RuntimeException("Profil formateur introuvable"));
            return seanceRepository.findByFormateurId(formateur.getId()).stream()
                    .map(SeanceMapper::toResponse)
                    .toList();
        }

        throw new RuntimeException("Consultation de l'emploi du temps non disponible pour ce rôle");
    }

    @Override
    @Transactional
    public SeanceResponse create(SeanceRequest request) {
        Seance seance = buildSeance(new Seance(), request);
        validateNoConflict(seance, null);
        Seance saved = seanceRepository.save(seance);
        notificationService.notifySeanceCreated(saved);
        return SeanceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SeanceResponse update(Long id, SeanceRequest request) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));
        Seance previousState = snapshotSeance(seance);
        buildSeance(seance, request);
        validateNoConflict(seance, id);
        Seance saved = seanceRepository.save(seance);
        notificationService.notifySeanceUpdated(saved, previousState);
        return SeanceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));
        notificationService.notifySeanceDeleted(seance);
        seanceRepository.delete(seance);
    }

    private Seance snapshotSeance(Seance source) {
        return Seance.builder()
                .id(source.getId())
                .formateur(source.getFormateur())
                .cours(source.getCours())
                .jourSemaine(source.getJourSemaine())
                .heureDebut(source.getHeureDebut())
                .heureFin(source.getHeureFin())
                .build();
    }

    private Seance buildSeance(Seance seance, SeanceRequest request) {
        if (request.getCoursId() == null && request.getFormationId() == null) {
            throw new RuntimeException("La formation est obligatoire");
        }
        if (request.getFormateurId() == null) {
            throw new RuntimeException("L'enseignant est obligatoire");
        }
        if (request.getJourSemaine() == null) {
            throw new RuntimeException("Le jour de la semaine est obligatoire");
        }

        EmploiDuTemps emploiDuTemps = resolveEmploiDuTemps(request);
        Cours cours = resolveCours(request);
        Formateur formateur = formateurRepository.findById(request.getFormateurId())
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));

        LocalTime debut = parseTime(request.getHeureDebut(), "heure de début");
        LocalTime fin = parseTime(request.getHeureFin(), "heure de fin");
        if (!debut.isBefore(fin)) {
            throw new RuntimeException("L'heure de fin doit être après l'heure de début");
        }

        seance.setEmploiDuTemps(emploiDuTemps);
        seance.setCours(cours);
        seance.setFormateur(formateur);
        seance.setJourSemaine(JourSemaine.fromIndex(request.getJourSemaine()));
        seance.setHeureDebut(debut);
        seance.setHeureFin(fin);
        seance.setSalle(request.getSalle());
        seance.setTypeSeance(parseTypeSeance(request.getTypeSeance()));

        return seance;
    }

    private EmploiDuTemps resolveEmploiDuTemps(SeanceRequest request) {
        if (request.getEmploiDuTempsId() != null) {
            return emploiDuTempsRepository.findById(request.getEmploiDuTempsId())
                    .orElseThrow(() -> new RuntimeException("Emploi du temps introuvable"));
        }

        if (request.getPromotionId() != null) {
            Promotion promotion = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new RuntimeException("Promotion introuvable"));
            return emploiDuTempsRepository.findByPromotionId(promotion.getId())
                    .orElseGet(() -> {
                        EmploiDuTemps created = EmploiDuTemps.builder()
                                .promotion(promotion)
                                .libelle("EDT — " + promotion.getNom())
                                .publie(false)
                                .build();
                        return emploiDuTempsRepository.save(created);
                    });
        }

        throw new RuntimeException("L'emploi du temps ou la promotion (classe) est obligatoire");
    }

    private Cours resolveCours(SeanceRequest request) {
        if (request.getCoursId() != null) {
            return coursRepository.findById(request.getCoursId())
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));
        }
        return resolveOrCreateCoursForFormation(request.getFormationId());
    }

    private Cours resolveOrCreateCoursForFormation(Long formationId) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        List<Cours> existing = coursRepository.findByFormationId(formationId);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        String code = "EDT-F" + formationId;
        return coursRepository.findByCode(code).orElseGet(() -> {
            String nom = formation.getTitre() != null && !formation.getTitre().isBlank()
                    ? formation.getTitre()
                    : formation.getNom();
            if (nom == null || nom.isBlank()) {
                nom = "Formation " + formationId;
            }

            Cours cours = Cours.builder()
                    .code(code)
                    .nom(nom)
                    .semestre("S1")
                    .coefficient(1.0)
                    .formation(formation)
                    .build();
            return coursRepository.save(cours);
        });
    }

    private void validateNoConflict(Seance seance, Long excludeId) {
        Formation formation = seance.getCours().getFormation();
        Long filiereId = formation.getFiliere() != null ? formation.getFiliere().getId() : null;
        NiveauEtude niveau = formation.getNiveau();

        List<Seance> formateurSessions = seanceRepository.findByFormateurId(seance.getFormateur().getId());
        for (Seance existing : formateurSessions) {
            if (excludeId != null && excludeId.equals(existing.getId())) {
                continue;
            }
            if (hasConflict(seance, existing)) {
                throw new RuntimeException(
                        "Conflit d'horaire : cet enseignant a déjà une séance le "
                                + formatJour(existing.getJourSemaine())
                                + " de "
                                + formatPlageHoraire(existing)
                                + " ("
                                + resolveCoursLabel(existing)
                                + ")."
                );
            }
        }

        if (filiereId != null && niveau != null) {
            List<Seance> cohortSessions = seanceRepository.findByFiliereAndNiveauAndJour(
                    filiereId,
                    niveau,
                    seance.getJourSemaine()
            );
            String filiereNom = formation.getFiliere().getNom();

            for (Seance existing : cohortSessions) {
                if (excludeId != null && excludeId.equals(existing.getId())) {
                    continue;
                }
                if (hasConflict(seance, existing)) {
                    throw new RuntimeException(
                            "Conflit d'horaire : une séance existe déjà pour la filière "
                                    + filiereNom
                                    + " ("
                                    + formatNiveau(niveau)
                                    + ") le "
                                    + formatJour(existing.getJourSemaine())
                                    + " de "
                                    + formatPlageHoraire(existing)
                                    + " avec "
                                    + resolveFormateurNom(existing.getFormateur())
                                    + "."
                    );
                }
            }
        }
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

    private String resolveFormateurNom(Formateur formateur) {
        Utilisateur utilisateur = formateur.getUtilisateur();
        if (utilisateur == null) {
            return "enseignant inconnu";
        }
        return (utilisateur.getPrenom() + " " + utilisateur.getNom()).trim();
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

    private String formatNiveau(NiveauEtude niveau) {
        return niveau.name().replace('_', ' ');
    }

    private boolean hasConflict(Seance a, Seance b) {
        if (!a.getJourSemaine().equals(b.getJourSemaine())) {
            return false;
        }
        return a.getHeureDebut().isBefore(b.getHeureFin())
                && b.getHeureDebut().isBefore(a.getHeureFin());
    }

    private LocalTime parseTime(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException("L'" + label + " est obligatoire");
        }
        try {
            return LocalTime.parse(value.length() == 5 ? value : value.substring(0, 5));
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Format horaire invalide pour " + label + " (attendu HH:mm)");
        }
    }

    private TypeSeance parseTypeSeance(String value) {
        if (value == null || value.isBlank()) {
            return TypeSeance.COURS;
        }
        try {
            return TypeSeance.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Type de séance invalide: " + value);
        }
    }
}
