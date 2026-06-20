package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FiliereModuleSummary {

    private Long id;
    private String titre;
    private String slug;
    private String description;
    private String imageUrl;
    private String niveau;
    private String typeFormation;
    private Integer subModuleCount;
}
