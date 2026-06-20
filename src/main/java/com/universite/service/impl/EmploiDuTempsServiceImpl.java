package com.universite.service.impl;

import com.universite.dto.EmploiDuTempsResponse;
import com.universite.dto.SeanceResponse;
import com.universite.entity.*;
import com.universite.mapper.EmploiDuTempsMapper;
import com.universite.mapper.PromotionMapper;
import com.universite.mapper.SeanceMapper;
import com.universite.repository.EmploiDuTempsRepository;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.FormateurRepository;
import com.universite.repository.PromotionRepository;
import com.universite.repository.SeanceRepository;
import com.universite.repository.UtilisateurRepository;
import com.universite.service.EmploiDuTempsService;
import com.universite.util.EtudiantProfileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmploiDuTempsServiceImpl implements EmploiDuTempsService {

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final FormateurRepository formateurRepository;
    private final SeanceRepository seanceRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public EmploiDuTempsResponse getByPromotion(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion introuvable"));
        return ensureForPromotion(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public EmploiDuTempsResponse getForCurrentEtudiant(String userEmail) {
        Etudiant etudiant = loadEtudiantByEmail(userEmail);
        return buildEmploiDuTempsForEtudiant(etudiant);
    }

    @Override
    @Transactional(readOnly = true)
    public EmploiDuTempsResponse getForEtudiant(Long etudiantId, String requesterEmail) {
        Utilisateur requester = utilisateurRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));

        assertCanViewEtudiantSchedule(requester, etudiant);
        return buildEmploiDuTempsForEtudiant(etudiant);
    }

    private Etudiant loadEtudiantByEmail(String userEmail) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return etudiantRepository.findByUtilisateur_Id(utilisateur.getId())
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));
    }

    private void assertCanViewEtudiantSchedule(Utilisateur requester, Etudiant etudiant) {
        if (requester.hasRole(RoleName.ETUDIANT)) {
            Etudiant self = etudiantRepository.findByUtilisateur_Id(requester.getId())
                    .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));
            if (!self.getId().equals(etudiant.getId())) {
                throw new RuntimeException("Accès non autorisé à cet emploi du temps");
            }
            return;
        }

        if (requester.hasRole(RoleName.ADMIN)
                || requester.hasRole(RoleName.RESPONSABLE_FORMATION)
                || requester.hasRole(RoleName.FORMATEUR)) {
            return;
        }

        throw new RuntimeException("Consultation de l'emploi du temps non disponible pour ce rôle");
    }

    private EmploiDuTempsResponse buildEmploiDuTempsForEtudiant(Etudiant etudiant) {
        Filiere filiere = EtudiantProfileUtils.resolveFiliere(etudiant);
        if (filiere == null) {
            throw new RuntimeException("Aucune filière associée à cet étudiant");
        }

        NiveauEtude niveau = etudiant.getNiveau();
        if (niveau == null) {
            throw new RuntimeException("Aucun niveau d'études associé à cet étudiant");
        }

        List<SeanceResponse> seances = seanceRepository
                .findByFormationFiliereIdAndNiveau(filiere.getId(), niveau)
                .stream()
                .sorted(Comparator
                        .comparing((Seance s) -> s.getJourSemaine().getIndex())
                        .thenComparing(Seance::getHeureDebut))
                .map(SeanceMapper::toResponse)
                .toList();

        Promotion promotion = etudiant.getPromotion();
        return EmploiDuTempsResponse.builder()
                .id(0L)
                .promotionId(promotion != null ? promotion.getId() : null)
                .promotionNom(promotion != null ? PromotionMapper.resolveTitre(promotion) : null)
                .libelle("Emploi du temps")
                .publie(true)
                .effectif(0L)
                .seances(seances)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeanceResponse> getSeancesForCurrentFormateur(String userEmail) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Formateur formateur = formateurRepository.findByUtilisateur_Id(utilisateur.getId())
                .orElseThrow(() -> new RuntimeException("Profil formateur introuvable"));

        return seanceRepository.findByFormateurId(formateur.getId()).stream()
                .map(SeanceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EmploiDuTempsResponse ensureForPromotion(Promotion promotion) {
        EmploiDuTemps emploiDuTemps = emploiDuTempsRepository.findByPromotionId(promotion.getId())
                .orElseGet(() -> emploiDuTempsRepository.save(
                        EmploiDuTemps.builder()
                                .promotion(promotion)
                                .libelle("EDT — " + promotion.getNom())
                                .publie(false)
                                .build()
                ));

        if (emploiDuTemps.getLibelle() == null || emploiDuTemps.getLibelle().isBlank()) {
            emploiDuTemps.setLibelle("EDT — " + promotion.getNom());
            emploiDuTempsRepository.save(emploiDuTemps);
        }

        return EmploiDuTempsMapper.toResponse(emploiDuTemps);
    }
}
