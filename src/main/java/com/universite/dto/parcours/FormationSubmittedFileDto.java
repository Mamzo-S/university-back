package com.universite.dto.parcours;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationSubmittedFileDto {
    private String id;
    private String name;
    private String size;
    private String uploadedAt;
}
