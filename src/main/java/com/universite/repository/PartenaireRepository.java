package com.universite.repository;

import com.universite.entity.Partenaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartenaireRepository extends JpaRepository<Partenaire, Long> {

    List<Partenaire> findAllByOrderByNomAsc();

    long countByActifTrue();
}
