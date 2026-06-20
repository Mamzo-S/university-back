package com.universite.dto;

import com.universite.entity.PorteeNotification;
import lombok.Data;

@Data
public class NotificationBroadcastRequest {
    private String titre;
    private String message;
    private PorteeNotification portee;
    private Long filiereId;
    private String niveau;
    private String lien;
}
