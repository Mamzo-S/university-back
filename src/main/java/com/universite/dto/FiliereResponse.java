package com.universite.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FiliereResponse {

    private Long id;

    private String nom;

    private String description;
}