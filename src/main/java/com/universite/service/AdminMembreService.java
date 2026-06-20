package com.universite.service;

import com.universite.dto.MembreResponse;

import java.util.List;

public interface AdminMembreService {

    List<MembreResponse> listAdministrateurs();

    List<MembreResponse> listFormateurs();

    List<MembreResponse> listResponsablesFormation();

    List<MembreResponse> listTuteurs();

    List<MembreResponse> listServicesInsertion();

    List<MembreResponse> listEtudiants();
}
