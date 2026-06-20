package com.universite.controller;

import com.universite.dto.NotificationBroadcastRequest;
import com.universite.dto.NotificationResponse;
import com.universite.dto.NotificationUnreadCountResponse;
import com.universite.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FORMATEUR', 'ETUDIANT')")
    public List<NotificationResponse> listMine(Authentication authentication) {
        return notificationService.listForCurrentUser(authentication.getName());
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyAuthority('FORMATEUR', 'ETUDIANT')")
    public NotificationUnreadCountResponse unreadCount(Authentication authentication) {
        return notificationService.countUnreadForCurrentUser(authentication.getName());
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyAuthority('FORMATEUR', 'ETUDIANT')")
    public NotificationResponse markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return notificationService.markAsRead(id, authentication.getName());
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasAnyAuthority('FORMATEUR', 'ETUDIANT')")
    public Map<String, String> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return Map.of("status", "ok");
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RESPONSABLE_FORMATION')")
    public Map<String, Integer> broadcast(@RequestBody NotificationBroadcastRequest request) {
        int count = notificationService.broadcastToStudents(request);
        return Map.of("notifiedCount", count);
    }
}
