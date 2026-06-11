package com.universite.repository;

import com.universite.entity.Role;
import com.universite.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNom(RoleName nom);
}
