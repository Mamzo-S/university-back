package com.universite.dto.parcours;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationQuizDto {
    private String id;
    private String title;
    private String description;
    private Integer questionsCount;
    private String duration;
    private String status;
    private Integer score;
}
