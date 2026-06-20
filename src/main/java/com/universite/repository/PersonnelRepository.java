package com.universite.repository;

import com.universite.entity.Personnel;
import com.universite.entity.TypePersonnel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonnelRepository extends JpaRepository<Personnel, Long> {

    Optional<Personnel> findByUtilisateur_Id(Long utilisateurId);

    List<Personnel> findByType(TypePersonnel type);
}
