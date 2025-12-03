package com.buzzlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_read", columnList = "user_id,is_read"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who will receive this notification

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actor_id") // The user who triggered this notification
    private User actor;

    // Reference to the entity that triggered this notification
    @Column(name = "channel_id")
    private Long channelId;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "dm_id")
    private Long directMessageId;

    @Column(name = "workspace_id")
    private Long workspaceId;

    @Column(nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        CHANNEL_MESSAGE,      // New message in a channel
        DIRECT_MESSAGE,       // New direct message
        THREAD_REPLY,         // Reply to your message
        REACTION,             // Someone reacted to your message
        MENTION,              // Someone mentioned you
        WORKSPACE_INVITE      // Invited to a workspace
    }
}
