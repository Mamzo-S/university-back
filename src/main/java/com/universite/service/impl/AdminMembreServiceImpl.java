package com.universite.service.impl;

import com.universite.dto.MembreResponse;
import com.universite.entity.RoleName;
import com.universite.entity.TypePersonnel;
import com.universite.entity.Utilisateur;
import com.universite.mapper.EtudiantMapper;
import com.universite.mapper.MembreMapper;
import com.universite.repository.EtudiantRepository;
import com.universite.repository.FormateurRepository;
import com.universite.repository.PersonnelRepository;
import com.universite.repository.UtilisateurRepository;
import com.universite.service.AdminMembreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMembreServiceImpl implements AdminMembreService {

    private final UtilisateurRepository utilisateurRepository;
    private final PersonnelRepository personnelRepository;
    private final FormateurRepository formateurRepository;
    private final EtudiantRepository etudiantRepository;

    @Override
    public List<MembreResponse> listAdministrateurs() {
        List<MembreResponse> membres = new ArrayList<>();

        for (Utilisateur utilisateur : utilisateurRepository.findByRoleNom(RoleName.ADMIN)) {
            membres.add(MembreMapper.fromUtilisateur(utilisateur, RoleName.ADMIN));
        }

        personnelRepository.findByType(TypePersonnel.PERSONNEL_ADMIN).stream()
                .map(MembreMapper::fromPersonnel)
                .forEach(membres::add);

        return membres;
    }

    @Override
    public List<MembreResponse> listFormateurs() {
        return formateurRepository.findAllWithFormations().stream()
                .map(MembreMapper::fromFormateur)
                .toList();
    }

    @Override
    public List<MembreResponse> listResponsablesFormation() {
        return personnelRepository.findByType(TypePersonnel.RESPONSABLE_FORMATION).stream()
                .map(MembreMapper::fromPersonnel)
                .toList();
    }

    @Override
    public List<MembreResponse> listTuteurs() {
        return personnelRepository.findByType(TypePersonnel.TUTEUR).stream()
                .map(MembreMapper::fromPersonnel)
                .toList();
    }

    @Override
    public List<MembreResponse> listServicesInsertion() {
        return personnelRepository.findByType(TypePersonnel.SERVICE_INSERTION).stream()
                .map(MembreMapper::fromPersonnel)
                .toList();
    }

    @Override
    public List<MembreResponse> listEtudiants() {
        return etudiantRepository.findAll().stream()
                .map(EtudiantMapper::toDTO)
                .map(MembreMapper::fromEtudiant)
                .toList();
    }
}
