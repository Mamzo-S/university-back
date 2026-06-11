package com.universite.service;

import com.universite.entity.Formation;

import java.util.List;

public interface FormationService {

    Formation ajouterFormation(Formation formation);

    List<Formation> getAllFormations();
}

