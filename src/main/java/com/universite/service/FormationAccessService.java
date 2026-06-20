package com.universite.service;

import com.universite.entity.Formation;

public interface FormationAccessService {

    void assertCanReadFormation(Formation formation, String userEmail);

    void assertCanManageFormation(Formation formation, String userEmail);

    void assertCanDeleteFormation(Formation formation, String userEmail);

    void assertCanUpdateParcours(Formation formation, String userEmail);
}
