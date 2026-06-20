package com.universite.repository;

import com.universite.entity.RoleName;
import com.universite.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    @Query("""
            SELECT DISTINCT u FROM Utilisateur u
            JOIN u.roles r
            WHERE r.nom = :roleName
            """)
    List<Utilisateur> findByRoleNom(@Param("roleName") RoleName roleName);
}

