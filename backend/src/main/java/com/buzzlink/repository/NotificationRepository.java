package com.buzzlink.repository;

import com.buzzlink.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications for a user, ordered by most recent first
    List<Notification> findByUserClerkIdOrderByCreatedAtDesc(String clerkId);

    // Get unread notifications for a user
    List<Notification> findByUserClerkIdAndIsReadFalseOrderByCreatedAtDesc(String clerkId);

    // Count unread notifications for a user
    Long countByUserClerkIdAndIsReadFalse(String clerkId);

    // Mark a notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId AND n.user.clerkId = :clerkId")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("clerkId") String clerkId);

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.clerkId = :clerkId AND n.isRead = false")
    int markAllAsRead(@Param("clerkId") String clerkId);

    // Delete old read notifications (optional cleanup)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.clerkId = :clerkId AND n.isRead = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("clerkId") String clerkId, @Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
