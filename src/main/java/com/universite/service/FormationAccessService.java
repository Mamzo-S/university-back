package com.universite.service;

import com.universite.entity.Formation;

public interface FormationAccessService {

    void assertCanReadFormation(Formation formation, String userEmail);
}
