package com.universite.repository;

import com.universite.entity.EmploiDuTemps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {

    Optional<EmploiDuTemps> findByPromotionId(Long promotionId);
}
