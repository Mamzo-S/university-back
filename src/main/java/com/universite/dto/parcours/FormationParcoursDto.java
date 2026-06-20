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
public class FormationParcoursDto {
    private String managerName;
    private String trainerName;
    private String tutorName;
    private String duration;
    private String sessionUrl;
    @Builder.Default
    private List<FormationSubModuleDto> subModules = new ArrayList<>();
    @Builder.Default
    private List<FormationProjectDepositDto> projectDeposits = new ArrayList<>();
}
