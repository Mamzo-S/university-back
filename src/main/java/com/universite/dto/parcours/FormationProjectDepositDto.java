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
public class FormationProjectDepositDto {
    private String id;
    private String title;
    private String description;
    private String opensAt;
    private String deadline;
    private Integer maxFiles;
    private Integer maxFileSizeMb;
    @Builder.Default
    private List<String> allowedExtensions = new ArrayList<>();
    @Builder.Default
    private List<FormationSubmittedFileDto> submittedFiles = new ArrayList<>();
}
