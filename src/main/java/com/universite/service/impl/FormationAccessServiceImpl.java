package com.universite.service.impl;

import com.universite.entity.Etudiant;
import com.universite.entity.Filiere;
import com.universite.entity.Formation;
import com.universite.entity.NiveauEtude;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.FormationRepository;
import com.universite.service.FormationAccessService;
import com.universite.util.EtudiantProfileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FormationAccessServiceImpl implements FormationAccessService {

    private final EtudiantRepository etudiantRepository;
    private final FormationRepository formationRepository;

    @Override
    public void assertCanReadFormation(Formation formation, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return;
        }

        Etudiant etudiant = etudiantRepository.findByUtilisateur_EmailWithProfile(userEmail).orElse(null);
        if (etudiant == null) {
            return;
        }

        Filiere filiere = EtudiantProfileUtils.resolveFiliere(etudiant);
        NiveauEtude niveauEtudiant = etudiant.getNiveau();
        List<Formation> formationsByScope = filiere != null
                ? (niveauEtudiant != null
                ? formationRepository.findByFiliereIdAndNiveau(filiere.getId(), niveauEtudiant)
                : formationRepository.findByFiliereId(filiere.getId()))
                : List.of();

        List<Formation> accessible = EtudiantProfileUtils.mergeAccessibleModules(etudiant, formationsByScope);
        boolean allowed = accessible.stream().anyMatch(item -> item.getId().equals(formation.getId()));
        if (!allowed) {
            throw new AccessDeniedException("Accès refusé à ce module");
        }
    }
}
