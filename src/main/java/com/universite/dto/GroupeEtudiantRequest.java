package com.universite.dto;

import lombok.Data;

@Data
public class GroupeEtudiantRequest {

    private String titre;
    private String slug;
    private String description;
    private Long promotionId;
}
