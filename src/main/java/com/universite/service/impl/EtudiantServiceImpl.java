package com.universite.service.impl;

import com.universite.dto.CreateEtudiantRequest;
import com.universite.dto.EtudiantDTO;
import com.universite.dto.EtudiantFiliereView;
import com.universite.dto.FiliereModuleSummary;
import com.universite.dto.MembreResponse;
import com.universite.dto.UpdateEtudiantRequest;
import com.universite.entity.*;
import com.universite.export.ExcelExporter;
import com.universite.mapper.EtudiantMapper;
import com.universite.mapper.FormationMapper;
import com.universite.mapper.MembreMapper;
import com.universite.repository.*;
import com.universite.service.EtudiantService;
import com.universite.util.EtudiantProfileUtils;
import com.universite.util.NiveauEtudeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtudiantServiceImpl implements EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PromotionRepository promotionRepository;
    private final FormateurRepository formateurRepository;
    private final GroupeEtudiantRepository groupeEtudiantRepository;
    private final FiliereRepository filiereRepository;
    private final FormationRepository formationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Etudiant ajouterEtudiant(Etudiant etudiant) {
        if (etudiantRepository.findByIne(etudiant.getIne()).isPresent()) {
            throw new RuntimeException("INE déjà utilisé");
        }

        if (etudiant.getUtilisateur() == null) {
            throw new RuntimeException("Un utilisateur doit être associé à l'étudiant");
        }

        if (etudiantRepository.findByUtilisateur_Id(etudiant.getUtilisateur().getId()).isPresent()) {
            throw new RuntimeException("Cet utilisateur possède déjà un profil étudiant");
        }

        return etudiantRepository.save(etudiant);
    }

    @Override
    @Transactional
    public MembreResponse createEtudiant(CreateEtudiantRequest request, String creatorEmail) {
        validateCreateRequest(request);

        if (utilisateurRepository.findByEmail(request.getEmail().trim()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        if (etudiantRepository.findByIne(request.getIne().trim()).isPresent()) {
            throw new RuntimeException("INE déjà utilisé");
        }

        Promotion promotion = resolvePromotion(request.getPromotionId());
        Filiere filiere = resolveFiliere(request.getFiliereId());
        assertCanManageFiliere(creatorEmail, filiere);
        if (promotion != null) {
            assertCanManagePromotion(creatorEmail, promotion);
        }

        Role etudiantRole = roleRepository.findByNom(RoleName.ETUDIANT)
                .orElseThrow(() -> new RuntimeException("Rôle ETUDIANT introuvable"));

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom().trim())
                .prenom(request.getPrenom().trim())
                .email(request.getEmail().trim())
                .telephone(trimOrNull(request.getTelephone()))
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .actif(true)
                .roles(new HashSet<>())
                .build();
        utilisateur.addRole(etudiantRole);
        utilisateurRepository.save(utilisateur);

        Etudiant etudiant = Etudiant.builder()
                .ine(request.getIne().trim())
                .dateNaissance(resolveDateNaissance(request.getDateNaissance()))
                .niveau(NiveauEtudeParser.parseRequired(request.getNiveau()))
                .utilisateur(utilisateur)
                .filiere(filiere)
                .promotion(promotion)
                .groupeEtudiant(resolveGroupe(request.getGroupeEtudiantId(), promotion))
                .build();

        etudiantRepository.save(etudiant);
        return MembreMapper.fromEtudiant(EtudiantMapper.toDTO(etudiant));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembreResponse> listEtudiantsForUser(String userEmail) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Etudiant> etudiants;
        if (canViewAllStudents(utilisateur)) {
            etudiants = etudiantRepository.findAllWithDetails();
        } else if (utilisateur.hasRole(RoleName.FORMATEUR)) {
            Set<Long> filiereIds = loadFormateurFiliereIds(userEmail);
            if (filiereIds.isEmpty()) {
                return List.of();
            }
            etudiants = etudiantRepository.findByFiliereIds(filiereIds.stream().toList());
        } else {
            throw new RuntimeException("Accès refusé à la liste des étudiants");
        }

        return mapEtudiantsToMembreResponses(etudiants);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembreResponse> listEtudiantsByModuleForUser(String userEmail, Long moduleId) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (canViewAllStudents(utilisateur)) {
            return listEtudiantsByModule(moduleId);
        }
        return listEtudiantsByModuleForFormateur(userEmail, moduleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembreResponse> listEtudiantsByModule(Long moduleId) {
        Formation module = formationRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));

        return mapEtudiantsToMembreResponses(loadEtudiantsByModuleScope(module));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembreResponse> listEtudiantsByModuleForFormateur(String userEmail, Long moduleId) {
        Formation module = formationRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));

        Set<Long> assignedModuleIds = loadFormateurFormationIds(userEmail);
        if (!assignedModuleIds.contains(moduleId)) {
            throw new RuntimeException("Ce module ne vous est pas assigné");
        }

        return mapEtudiantsToMembreResponses(loadEtudiantsByModuleScope(module));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembreResponse> listEtudiantsFiltered(
            String userEmail,
            Long filiereId,
            Long formationId,
            Long promotionId,
            Long groupeEtudiantId
    ) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!canViewAllStudents(utilisateur)) {
            throw new RuntimeException("Accès refusé à la liste des étudiants");
        }

        List<Etudiant> etudiants;
        if (groupeEtudiantId != null) {
            etudiants = etudiantRepository.findByGroupeEtudiantId(groupeEtudiantId);
        } else if (promotionId != null) {
            etudiants = etudiantRepository.findByPromotionId(promotionId);
        } else if (formationId != null) {
            Formation formation = formationRepository.findById(formationId)
                    .orElseThrow(() -> new RuntimeException("Formation introuvable"));
            etudiants = loadEtudiantsByModuleScope(formation);
        } else if (filiereId != null) {
            etudiants = etudiantRepository.findByFiliereId(filiereId);
        } else {
            etudiants = etudiantRepository.findAllWithDetails();
        }

        return mapEtudiantsToMembreResponses(etudiants);
    }

    private List<Etudiant> loadEtudiantsByModuleScope(Formation module) {
        Long filiereId = module.getFiliere() != null ? module.getFiliere().getId() : null;
        return etudiantRepository.findByModuleScope(module.getId(), filiereId, module.getNiveau());
    }

    @Override
    @Transactional
    public MembreResponse updateEtudiant(Long id, UpdateEtudiantRequest request) {
        Etudiant etudiant = getEtudiantById(id);
        Utilisateur utilisateur = etudiant.getUtilisateur();
        if (utilisateur == null) {
            throw new RuntimeException("Utilisateur introuvable pour cet étudiant");
        }

        if (request.getNom() != null && !request.getNom().isBlank()) {
            utilisateur.setNom(request.getNom().trim());
        }
        if (request.getPrenom() != null && !request.getPrenom().isBlank()) {
            utilisateur.setPrenom(request.getPrenom().trim());
        }
        if (request.getTelephone() != null) {
            utilisateur.setTelephone(trimOrNull(request.getTelephone()));
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim();
            utilisateurRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(utilisateur.getId())) {
                    throw new RuntimeException("Email déjà utilisé");
                }
            });
            utilisateur.setEmail(email);
        }

        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            if (request.getMotDePasse().length() < 6) {
                throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
            }
            utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        }

        if (request.getIne() != null && !request.getIne().isBlank()) {
            String ine = request.getIne().trim();
            etudiantRepository.findByIne(ine).ifPresent(existing -> {
                if (!existing.getId().equals(etudiant.getId())) {
                    throw new RuntimeException("INE déjà utilisé");
                }
            });
            etudiant.setIne(ine);
        }

        if (request.getDateNaissance() != null) {
            etudiant.setDateNaissance(
                    request.getDateNaissance().isBlank()
                            ? etudiant.getDateNaissance()
                            : resolveDateNaissance(request.getDateNaissance())
            );
        }

        if (request.getNiveau() != null && !request.getNiveau().isBlank()) {
            etudiant.setNiveau(NiveauEtudeParser.parseRequired(request.getNiveau()));
        } else if (etudiant.getNiveau() == null) {
            etudiant.setNiveau(NiveauEtude.LICENCE_1);
        }

        Promotion promotion = request.getPromotionId() != null
                ? resolvePromotion(request.getPromotionId())
                : etudiant.getPromotion();
        Filiere filiere = request.getFiliereId() != null
                ? resolveFiliere(request.getFiliereId())
                : etudiant.getFiliere();

        if (request.getFiliereId() != null) {
            etudiant.setFiliere(filiere);
        }
        if (request.getPromotionId() != null) {
            etudiant.setPromotion(promotion);
        }

        if (request.getGroupeEtudiantId() != null) {
            if (request.getGroupeEtudiantId() <= 0) {
                etudiant.setGroupeEtudiant(null);
            } else {
                etudiant.setGroupeEtudiant(resolveGroupe(request.getGroupeEtudiantId(), promotion));
            }
        } else if (request.getPromotionId() != null && etudiant.getGroupeEtudiant() != null
                && etudiant.getGroupeEtudiant().getPromotion() != null
                && promotion != null
                && !etudiant.getGroupeEtudiant().getPromotion().getId().equals(promotion.getId())) {
            etudiant.setGroupeEtudiant(null);
        }

        utilisateurRepository.save(utilisateur);
        etudiantRepository.save(etudiant);
        return MembreMapper.fromEtudiant(EtudiantMapper.toDTO(etudiant));
    }

    @Override
    @Transactional(readOnly = true)
    public EtudiantFiliereView getMyFiliereView(String userEmail) {
        Etudiant etudiant = etudiantRepository.findByUtilisateur_EmailWithProfile(userEmail)
                .orElseThrow(() -> new RuntimeException("Profil étudiant introuvable"));

        Filiere filiere = EtudiantProfileUtils.resolveFiliere(etudiant);

        if (filiere == null && (etudiant.getPromotion() == null || etudiant.getPromotion().getFormation() == null)) {
            throw new RuntimeException("Aucune filière associée à votre profil");
        }

        NiveauEtude niveauEtudiant = etudiant.getNiveau();
        List<Formation> formationsByScope = filiere != null
                ? (niveauEtudiant != null
                ? formationRepository.findByFiliereIdAndNiveau(filiere.getId(), niveauEtudiant)
                : formationRepository.findByFiliereId(filiere.getId()))
                : List.of();

        List<Formation> formations = EtudiantProfileUtils.mergeAccessibleModules(etudiant, formationsByScope);

        List<FiliereModuleSummary> modules = formations.stream()
                .sorted(Comparator.comparing(
                        FormationMapper::resolveTitre,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .map(formation -> FiliereModuleSummary.builder()
                        .id(formation.getId())
                        .titre(FormationMapper.resolveTitre(formation))
                        .slug(formation.getSlug())
                        .niveau(
                                formation.getNiveau() != null
                                        ? formation.getNiveau().name()
                                        : null
                        )
                        .typeFormation(formation.getTypeFormation())
                        .build())
                .toList();

        return EtudiantFiliereView.builder()
                .id(filiere != null ? filiere.getId() : null)
                .nom(filiere != null ? filiere.getNom() : null)
                .description(filiere != null ? filiere.getDescription() : null)
                .niveauEtudiant(niveauEtudiant != null ? niveauEtudiant.name() : null)
                .modules(modules)
                .build();
    }

    private List<MembreResponse> mapEtudiantsToMembreResponses(List<Etudiant> etudiants) {
        return etudiants.stream()
                .sorted(Comparator.comparing(
                        e -> e.getUtilisateur() != null ? e.getUtilisateur().getNom() : "",
                        String.CASE_INSENSITIVE_ORDER
                ))
                .map(EtudiantMapper::toDTO)
                .map(MembreMapper::fromEtudiant)
                .toList();
    }

    @Override
    public List<Etudiant> getAllEtudiants() {
        return etudiantRepository.findAll();
    }

    @Override
    public Etudiant getEtudiantById(Long id) {
        return etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etudiant introuvable"));
    }

    @Override
    @Deprecated
    public Etudiant modifierEtudiant(Long id, Etudiant etudiant) {
        throw new UnsupportedOperationException(
                "Endpoint obsolète : utilisez updateEtudiant avec UpdateEtudiantRequest"
        );
    }

    @Override
    public void supprimerEtudiant(Long id) {
        Etudiant etudiant = getEtudiantById(id);
        etudiantRepository.delete(etudiant);
    }

    @Override
    public Page<EtudiantDTO> getEtudiantsPagines(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return etudiantRepository.findAll(pageable).map(EtudiantMapper::toDTO);
    }

    @Override
    public Page<EtudiantDTO> rechercherParNom(String nom, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return etudiantRepository
                .findByUtilisateur_NomContainingIgnoreCase(nom, pageable)
                .map(EtudiantMapper::toDTO);
    }

    @Override
    public ByteArrayInputStream exportExcel() {
        List<Etudiant> etudiants = etudiantRepository.findAll();
        return ExcelExporter.exportEtudiants(etudiants);
    }

    private void validateCreateRequest(CreateEtudiantRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("L'email est obligatoire");
        }
        if (request.getMotDePasse() == null || request.getMotDePasse().length() < 6) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
        }
        if (request.getNom() == null || request.getNom().isBlank()) {
            throw new RuntimeException("Le nom est obligatoire");
        }
        if (request.getPrenom() == null || request.getPrenom().isBlank()) {
            throw new RuntimeException("Le prénom est obligatoire");
        }
        if (request.getIne() == null || request.getIne().isBlank()) {
            throw new RuntimeException("L'INE est obligatoire");
        }
        if (request.getFiliereId() == null) {
            throw new RuntimeException("La filière est obligatoire");
        }
        if (request.getNiveau() == null || request.getNiveau().isBlank()) {
            throw new RuntimeException("Le niveau est obligatoire");
        }
    }

    private Promotion resolvePromotion(Long promotionId) {
        if (promotionId == null) {
            return null;
        }
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion introuvable"));
    }

    private Filiere resolveFiliere(Long filiereId) {
        return filiereRepository.findById(filiereId)
                .orElseThrow(() -> new RuntimeException("Filière introuvable"));
    }

    private void assertCanManageFiliere(String creatorEmail, Filiere filiere) {
        Utilisateur creator = utilisateurRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (canViewAllStudents(creator)) {
            return;
        }

        if (!creator.hasRole(RoleName.FORMATEUR)) {
            throw new RuntimeException("Accès refusé");
        }

        Set<Long> filiereIds = loadFormateurFiliereIds(creatorEmail);
        if (!filiereIds.contains(filiere.getId())) {
            throw new RuntimeException(
                    "Vous ne pouvez inscrire un étudiant que dans les filières de vos modules assignés"
            );
        }
    }

    private GroupeEtudiant resolveGroupe(Long groupeId, Promotion promotion) {
        if (groupeId == null) {
            return null;
        }
        GroupeEtudiant groupe = groupeEtudiantRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe d'étudiants introuvable"));
        if (groupe.getPromotion() == null
                || promotion == null
                || !groupe.getPromotion().getId().equals(promotion.getId())) {
            throw new RuntimeException("Le groupe sélectionné n'appartient pas à cette promotion");
        }
        return groupe;
    }

    private void assertCanManagePromotion(String creatorEmail, Promotion promotion) {
        Utilisateur creator = utilisateurRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (canViewAllStudents(creator)) {
            return;
        }

        if (!creator.hasRole(RoleName.FORMATEUR)) {
            throw new RuntimeException("Accès refusé");
        }

        if (promotion.getFormation() == null) {
            return;
        }

        Set<Long> formationIds = loadFormateurFormationIds(creatorEmail);
        if (!formationIds.contains(promotion.getFormation().getId())) {
            throw new RuntimeException(
                    "Vous ne pouvez inscrire un étudiant que sur vos formations assignées"
            );
        }
    }

    private Set<Long> loadFormateurFormationIds(String userEmail) {
        Formateur formateur = formateurRepository.findWithFormationsByUtilisateur_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("Profil formateur introuvable"));

        if (formateur.getFormations() == null) {
            return Set.of();
        }

        return formateur.getFormations().stream()
                .map(Formation::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> loadFormateurFiliereIds(String userEmail) {
        Formateur formateur = formateurRepository.findWithFormationsByUtilisateur_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("Profil formateur introuvable"));

        if (formateur.getFormations() == null) {
            return Set.of();
        }

        return formateur.getFormations().stream()
                .map(Formation::getFiliere)
                .filter(filiere -> filiere != null && filiere.getId() != null)
                .map(Filiere::getId)
                .collect(Collectors.toSet());
    }

    private boolean canViewAllStudents(Utilisateur utilisateur) {
        return utilisateur.hasRole(RoleName.ADMIN)
                || utilisateur.hasRole(RoleName.RESPONSABLE_FORMATION)
                || utilisateur.hasRole(RoleName.SERVICE_INSERTION);
    }

    private LocalDate resolveDateNaissance(String dateNaissance) {
        if (dateNaissance != null && !dateNaissance.isBlank()) {
            try {
                return LocalDate.parse(dateNaissance);
            } catch (DateTimeParseException ex) {
                throw new RuntimeException("Date de naissance invalide (format attendu : AAAA-MM-JJ)");
            }
        }
        return LocalDate.of(2000, 1, 1);
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
