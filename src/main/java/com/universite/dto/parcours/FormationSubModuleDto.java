package com.universite.dto.parcours;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationSubModuleDto {
    private String id;
    private String title;
    private String description;
    private Integer order;
    @Builder.Default
    private List<FormationDocumentDto> documents = new ArrayList<>();
    @Builder.Default
    private List<FormationQuizDto> quizzes = new ArrayList<>();
    @Builder.Default
    private List<FormationResourceDto> resources = new ArrayList<>();
}
