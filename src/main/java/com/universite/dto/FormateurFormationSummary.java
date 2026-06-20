package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormateurFormationSummary {

    private Long id;
    private String titre;
    private String slug;
    private Long filiereId;
    private String filiereNom;
    private String description;
    private String imageUrl;
    private String niveau;
    private String typeFormation;
    private Integer subModuleCount;
    private boolean assignee;
    private boolean cree;
}
