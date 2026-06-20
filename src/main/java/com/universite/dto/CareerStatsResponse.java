package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CareerStatsResponse {

    private long internships;
    private long partners;
    private long employed;
    private long selfEmployed;
    private long pendingConventions;
    private long activeInternships;
}
