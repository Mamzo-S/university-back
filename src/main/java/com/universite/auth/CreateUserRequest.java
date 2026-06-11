package com.universite.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest {

    private String email;
    private String motDePasse;
    private String nom;
    private String prenom;
    private String role; // ADMIN, PROFESSEUR, ETUDIANT
}
