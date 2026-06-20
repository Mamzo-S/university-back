package com.universite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur destinataire;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false)
    @Builder.Default
    private Boolean lu = false;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(length = 255)
    private String lien;

    @Column(name = "reference_id")
    private Long referenceId;
}
