package com.universite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long users;
    private long students;
    private long formations;
    private long trainers;
    private long personnel;
    private long filieres;
    private long promotions;
}
