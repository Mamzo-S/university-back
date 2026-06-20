package com.universite.repository;

import com.universite.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n FROM Notification n
            WHERE n.destinataire.id = :utilisateurId
            ORDER BY n.dateCreation DESC
            """)
    List<Notification> findByDestinataireIdOrderByDateCreationDesc(
            @Param("utilisateurId") Long utilisateurId
    );

    long countByDestinataireIdAndLuFalse(Long utilisateurId);

    Optional<Notification> findByIdAndDestinataireId(Long id, Long utilisateurId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.lu = true
            WHERE n.destinataire.id = :utilisateurId
              AND n.lu = false
            """)
    int markAllAsRead(@Param("utilisateurId") Long utilisateurId);
}
