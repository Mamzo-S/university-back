package com.universite.service;

import com.universite.dto.NotificationBroadcastRequest;
import com.universite.dto.NotificationResponse;
import com.universite.dto.NotificationUnreadCountResponse;
import com.universite.entity.Etudiant;
import com.universite.entity.Formateur;
import com.universite.entity.Seance;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> listForCurrentUser(String userEmail);

    NotificationUnreadCountResponse countUnreadForCurrentUser(String userEmail);

    NotificationResponse markAsRead(Long notificationId, String userEmail);

    void markAllAsRead(String userEmail);

    int broadcastToStudents(NotificationBroadcastRequest request);

    void notifySeanceCreated(Seance seance);

    void notifySeanceUpdated(Seance seance, Seance previousState);

    void notifySeanceDeleted(Seance seance);

    void notifyFormateurModulesAssigned(Formateur formateur, int moduleCount);

    void notifyBulletinPublished(Etudiant etudiant, String semestre, String anneeAcademique);
}
