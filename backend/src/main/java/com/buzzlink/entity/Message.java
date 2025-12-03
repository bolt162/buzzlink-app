package com.buzzlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Message entity representing a chat message in a channel.
 */
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_channel_created", columnList = "channel_id,created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Channel this message belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    /**
     * User who sent this message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Message content (text)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Message type - TEXT or FILE
     * FILE type messages have content as a URL
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    /**
     * Parent message ID for threaded replies
     * Null for top-level messages
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private Message parentMessage;

    /**
     * Number of replies to this message
     */
    @Column(nullable = false)
    private Integer replyCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum MessageType {
        TEXT,
        FILE
    }
}
