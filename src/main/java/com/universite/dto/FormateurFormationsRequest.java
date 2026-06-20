package com.universite.dto;

import lombok.Data;

import java.util.List;

@Data
public class FormateurFormationsRequest {

    private List<Long> formationIds;
}
