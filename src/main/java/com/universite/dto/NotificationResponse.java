package com.universite.dto;

import com.universite.entity.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String titre;
    private String message;
    private NotificationType type;
    private Boolean lu;
    private LocalDateTime dateCreation;
    private String lien;
    private Long referenceId;
}
