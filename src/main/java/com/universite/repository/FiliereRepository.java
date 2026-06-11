package com.universite.repository;

import com.universite.entity.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiliereRepository
        extends JpaRepository<Filiere, Long> {
}